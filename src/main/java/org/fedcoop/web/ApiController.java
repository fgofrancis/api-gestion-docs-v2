package org.fedcoop.web;
import jakarta.validation.constraints.*;
import org.fedcoop.domain.*;
import org.fedcoop.repository.*;

import org.fedcoop.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.*; import java.time.LocalDate; import java.util.*;

@RestController @RequestMapping("/api")
public class ApiController {
  record LoginReq(@NotBlank String email, @NotBlank String password) {}
  record DocDto(Long id,String title,String description,LocalDate docDate,String type,Long typeId,String org,Long orgId,String fileName,long size,String uploadedBy,java.time.Instant createdAt,boolean canDelete){}
  private final OrgRepo orgs;
  private final UserRepo users;
  private final TypeRepo types;
  private final DocRepo docs;
  private final PasswordEncoder enc;
  private final JwtService jwt;
  private final Path dir;

  public ApiController(OrgRepo o,UserRepo u,TypeRepo t,DocRepo d,PasswordEncoder e,JwtService j,@Value("${app.storage-dir}") String sd) throws IOException {
    orgs=o;users=u;types=t;docs=d;enc=e;jwt=j; dir=Files.createDirectories(Path.of(sd)); }

  @PostMapping("/auth/login")
  Map<String,Object> login(@RequestBody LoginReq r){
    AppUser u = users.findByEmail(r.email()).filter(x->x.isActive()&&x.getOrg().isActive()&&enc.matches(r.password(),x.getPasswordHash()))
      .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales inválidas"));

    return Map.of("token",jwt.issue(u),"name",u.getFullName(),"org",u.getOrg().getName(),
      "federation",u.getOrg().getKind()==Organization.Kind.FEDERATION,"admin",u.getRole()==AppUser.Role.ADMIN);
  }

  /** Tipos disponibles: las cooperativas no ven los tipos exclusivos de la Federación. */
  @GetMapping("/types") List<DocType> types(){
    var c=Ctx.current();
    return types.findAll().stream().filter(t->c.federation()||!t.isFederationOnly()).toList();
  }

  @GetMapping("/organizations") List<Organization> orgs(){
    if(!Ctx.current().federation()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return orgs.findAll(); }

  @GetMapping("/documents")
  Page<DocDto> list(@RequestParam(required=false) Long orgId,@RequestParam(required=false) Long typeId,
                    @RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int page,
                    @RequestParam(defaultValue="10") int size)
  {
    var c = Ctx.current();
    Long scope = c.federation()? orgId : c.orgId();   // AISLAMIENTO MULTI-TENANT
    return docs.search(scope,typeId,q,PageRequest.of(page,size,Sort.by("createdAt").descending())).map(d->dto(d,c));
  }

  @PostMapping(value="/documents",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  DocDto upload(@RequestParam String title,@RequestParam(required=false) String description,@RequestParam Long typeId,
                @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate docDate,
                @RequestParam MultipartFile file) throws IOException {
    var c = Ctx.current();
    if(title.isBlank()||file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Título y archivo son obligatorios");

    DocType t = types.findById(typeId).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tipo inválido"));
    if(t.isFederationOnly() && !c.federation()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Tipo exclusivo de la Federación");

    Doc d = new Doc(); d.setTitle(title.trim()); d.setDescription(description); d.setDocDate(docDate); d.setType(t);
    // d.setOrg(orgs.getReferenceById(c.orgId()));                      // el tenant sale del token, nunca del cliente
    d.setOrg(orgs.findById(c.orgId()).orElseThrow());
    d.setFileName(file.getOriginalFilename()); d.setContentType(file.getContentType()); d.setSize(file.getSize()); d.setUploadedBy(c.email());
    d.setStoredName(UUID.randomUUID().toString()); Files.copy(file.getInputStream(),dir.resolve(d.getStoredName()));
    return dto(docs.save(d),c);
  }

  @GetMapping("/documents/{id}/file")
  ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
    Doc d = visible(id);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(d.getContentType()==null?"application/octet-stream":d.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(d.getFileName()).build().toString())
      .body(new UrlResource(dir.resolve(d.getStoredName()).toUri()));
  }

  @DeleteMapping("/documents/{id}")
  void delete(@PathVariable Long id) throws IOException {
    var c=Ctx.current();
    Doc d=visible(id);
    if(!d.getOrg().getId().equals(c.orgId())||!c.admin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Solo un administrador de la organización propietaria puede eliminar");
    Files.deleteIfExists(dir.resolve(d.getStoredName()));
    docs.delete(d);
  }

  private Doc visible(Long id){
    var c=Ctx.current();
    Doc d=docs.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));

    if(!c.federation() && !d.getOrg().getId().equals(c.orgId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return d;
  }

  private DocDto dto(Doc d,Ctx c){
    return new DocDto(d.getId(),d.getTitle(),d.getDescription(),d.getDocDate(),d.getType().getName(),d.getType().getId(),
    d.getOrg().getName(),d.getOrg().getId(),d.getFileName(),d.getSize(),d.getUploadedBy(),d.getCreatedAt(),
            c.admin()&&d.getOrg().getId().equals(c.orgId())); }

  // Cuando en este controlador se lance una ResponseStatusException, ejecuta este método. Este luce estar de mas.
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String,String>> err(ResponseStatusException e){

    System.out.println("HANDLER DEL APICONTROLLER");
    return ResponseEntity.status(e.getStatusCode())
            .body(Map.of("message",e.getReason()==null?"Error":e.getReason())); }
}

// Nota: este metodo err, centraliza el manejo de errores del controlador y transforma cualquier
//  ResponseStatusException en una respuesta JSON consistente con la forma: