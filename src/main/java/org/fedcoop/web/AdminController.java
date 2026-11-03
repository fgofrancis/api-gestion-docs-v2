package org.fedcoop.web;
import org.fedcoop.domain.*;
import org.fedcoop.repository.*;

import org.fedcoop.security.Ctx;
import org.springframework.http.HttpStatus; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Administración de usuarios y cooperativas. Federación: todo. Cooperativa: solo los usuarios de su organización. */
@RestController @RequestMapping("/api/admin") @Transactional
public class AdminController {
  record UserDto(Long id,String email,String fullName,String role,boolean active,Long orgId,String orgName,boolean self){}
  record UserReq(String email,String fullName,String password,String role,Long orgId,Boolean active){}
  record OrgDto(Long id,String name,boolean active,long users){}
  record OrgReq(String name,Boolean active){}
  record PassReq(String password){}
  private final OrgRepo orgs; private final UserRepo users; private final PasswordEncoder enc;
  public AdminController(OrgRepo o,UserRepo u,PasswordEncoder e){ orgs=o; users=u; enc=e; }

  private Ctx admin(){ var c=Ctx.current(); if(!c.admin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Se requiere rol de administrador"); return c; }
  private Ctx fedAdmin(){ var c=admin(); if(!c.federation()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Solo la Federación puede gestionar cooperativas"); return c; }
  private ResponseStatusException bad(String m){ return new ResponseStatusException(HttpStatus.BAD_REQUEST,m); }
  private UserDto dto(AppUser u,Ctx c){ return new UserDto(u.getId(),u.getEmail(),u.getFullName(),u.getRole().name(),u.isActive(),u.getOrg().getId(),u.getOrg().getName(),u.getId().equals(c.uid())); }
  private AppUser target(Long id,Ctx c){
    AppUser u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    if(!c.federation() && !u.getOrg().getId().equals(c.orgId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);   // aislamiento
    return u; }
  private void checkPass(String p){ if(p==null||p.length()<8) throw bad("La contraseña debe tener al menos 8 caracteres"); }

  // ---------- Usuarios ----------
  @GetMapping("/users")
  List<UserDto> listUsers(@RequestParam(required=false) Long orgId){
    var c=admin();
    var list = !c.federation()? users.findByOrgIdOrderByFullName(c.orgId()) : orgId!=null? users.findByOrgIdOrderByFullName(orgId) : users.findAllByOrderByFullName();
    return list.stream().map(u->dto(u,c)).toList(); }

  @PostMapping("/users")
  UserDto createUser(@RequestBody UserReq r){
    var c=admin();
    if(r.email()==null||!r.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw bad("Correo inválido");
    if(r.fullName()==null||r.fullName().isBlank()) throw bad("El nombre es obligatorio");
    checkPass(r.password());
    String mail=r.email().trim().toLowerCase();
    if(users.findByEmail(mail).isPresent()) throw bad("Ya existe un usuario con ese correo");
    Long orgId = c.federation() && r.orgId()!=null ? r.orgId() : c.orgId();       // una cooperativa solo crea en la suya
    Organization org=orgs.findById(orgId).orElseThrow(()->bad("Organización inválida"));
    var u=new AppUser(); u.setEmail(mail); u.setFullName(r.fullName().trim()); u.setOrg(org);
    u.setRole("ADMIN".equals(r.role())?AppUser.Role.ADMIN:AppUser.Role.USER); u.setPasswordHash(enc.encode(r.password()));
    return dto(users.save(u),c); }

  @PutMapping("/users/{id}")
  UserDto updateUser(@PathVariable Long id,@RequestBody UserReq r){
    var c=admin(); var u=target(id,c);
    var newRole = r.role()!=null ? AppUser.Role.valueOf(r.role()) : u.getRole();
    boolean newActive = r.active()!=null ? r.active() : u.isActive();
    if(u.getId().equals(c.uid()) && (!newActive || newRole!=AppUser.Role.ADMIN)) throw bad("No puedes desactivarte ni quitarte el rol de administrador a ti mismo");
    if(r.fullName()!=null && !r.fullName().isBlank()) u.setFullName(r.fullName().trim());
    u.setRole(newRole); u.setActive(newActive);
    return dto(u,c); }

  @PostMapping("/users/{id}/password")
  void resetPassword(@PathVariable Long id,@RequestBody PassReq r){ var c=admin(); var u=target(id,c); checkPass(r.password()); u.setPasswordHash(enc.encode(r.password())); }

  // ---------- Cooperativas (solo Federación) ----------
  @GetMapping("/cooperatives")
  List<OrgDto> listCoops(){ fedAdmin();
    return orgs.findByKind(Organization.Kind.COOPERATIVE).stream().map(o->new OrgDto(o.getId(),o.getName(),o.isActive(),users.countByOrgId(o.getId()))).toList(); }

  @PostMapping("/cooperatives")
  OrgDto createCoop(@RequestBody OrgReq r){ fedAdmin();
    if(r.name()==null||r.name().isBlank()) throw bad("El nombre es obligatorio");
    var o=orgs.save(new Organization(null,r.name().trim(),Organization.Kind.COOPERATIVE)); return new OrgDto(o.getId(),o.getName(),true,0); }

  @PutMapping("/cooperatives/{id}")
  OrgDto updateCoop(@PathVariable Long id,@RequestBody OrgReq r){ fedAdmin();
    var o=orgs.findById(id).filter(x->x.getKind()==Organization.Kind.COOPERATIVE).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    if(r.name()!=null && !r.name().isBlank()) o.setName(r.name().trim());
    if(r.active()!=null) o.setActive(r.active());   // desactivar bloquea el ingreso de todos sus usuarios
    return new OrgDto(o.getId(),o.getName(),o.isActive(),users.countByOrgId(o.getId())); }
}
