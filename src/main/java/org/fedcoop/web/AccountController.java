package org.fedcoop.web;
import org.fedcoop.repository.*;
import org.fedcoop.security.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

/** Cambio de contraseña del propio usuario (único endpoint permitido mientras tenga clave temporal). */
@RestController @RequestMapping("/api/account") @Transactional
public class AccountController {
    record ChangeReq(String currentPassword,String newPassword){}
    private final UserRepo users; private final PasswordEncoder enc; private final JwtService jwt;
    public AccountController(UserRepo u,PasswordEncoder e,JwtService j){ users=u; enc=e; jwt=j; }

    @PostMapping("/change-password")
    Map<String,String> change(@RequestBody ChangeReq r){
        var u = users.findById(Ctx.current().uid())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if(r.currentPassword()==null || !enc.matches(r.currentPassword(),u.getPasswordHash())) throw bad("La contraseña actual no es correcta");

        String n = r.newPassword();
        if(n==null || n.length()<8 || !n.matches(".*[A-Za-z].*") || !n.matches(".*\\d.*")) throw bad("La nueva contraseña debe tener al menos 8 caracteres, con letras y números");

        if(enc.matches(n,u.getPasswordHash())) throw bad("La nueva contraseña debe ser distinta de la temporal");

        u.setPasswordHash(enc.encode(n)); u.setMustChangePassword(false);
        return Map.of("token",jwt.issue(u));          // token nuevo, ya sin la restricción
    }

    private ResponseStatusException bad(String m){
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);
    }
}
