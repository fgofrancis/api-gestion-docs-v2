package org.fedcoop.web;
import java.util.Map; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice //Esta anotación convierte la clase en un bean gestionado por Spring.
// Gestiona errores para todos los controladores REST de la aplicación.
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String,String>> err(ResponseStatusException e){

    System.out.println("HANDLER GLOBAL");
    return ResponseEntity.status(e.getStatusCode())
            .body(Map.of("message",e.getReason()==null?"Error":e.getReason())); }
}
