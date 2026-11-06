package org.fedcoop.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.fedcoop.domain.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.util.*;
@Service
public class JwtService {
  private final SecretKey key;
  public JwtService(@Value("${app.jwt-secret}") String s){ key = Keys.hmacShaKeyFor(s.getBytes()); }
  public String issue(AppUser u){
    return Jwts.builder().subject(u.getEmail())
      .claim("uid",u.getId()).claim("org",u.getOrg().getId())
      .claim("fed",u.getOrg().getKind()==Organization.Kind.FEDERATION)
      .claim("adm",u.getRole()==AppUser.Role.ADMIN).claim("mcp",u.isMustChangePassword())
      .expiration(new Date(System.currentTimeMillis()+8*3600_000L)).signWith(key).compact();
  }

  public Ctx parse(String t){
    Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload();
    return new Ctx(c.get("uid",Long.class), c.get("org",Long.class), c.get("fed",Boolean.class),
            c.get("adm",Boolean.class), c.getSubject(), Boolean.TRUE.equals(c.get("mcp",Boolean.class)));
  }

}
