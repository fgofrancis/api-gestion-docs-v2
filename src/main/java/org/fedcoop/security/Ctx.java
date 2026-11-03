package org.fedcoop.security;
import org.springframework.security.core.context.SecurityContextHolder;
/** Contexto del usuario autenticado: de aquí sale el tenant (orgId). */
public record Ctx(Long uid, Long orgId, boolean federation, boolean admin, String email) {
  public static Ctx current(){
    return (Ctx) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
