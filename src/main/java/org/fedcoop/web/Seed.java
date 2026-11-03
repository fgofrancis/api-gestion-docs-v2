package org.fedcoop.web;
import org.fedcoop.domain.*;
import org.fedcoop.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.password.PasswordEncoder;
/** Datos de demostración (contraseña de todos: demo1234). */
@Configuration
public class Seed {
  @Bean CommandLineRunner init(OrgRepo o,UserRepo u,TypeRepo t,PasswordEncoder e){ return a->{
    if(o.count()>0) return;
    var fed=o.save(new Organization(null,"Federación de Cooperativas",Organization.Kind.FEDERATION));
    var c1=o.save(new Organization(null,"Cooperativa Norte",Organization.Kind.COOPERATIVE));
    var c2=o.save(new Organization(null,"Cooperativa Sur",Organization.Kind.COOPERATIVE));
    user(u,e,"admin@federacion.org","Admin Federación",AppUser.Role.ADMIN,fed);
    user(u,e,"admin@norte.coop","Admin Norte",AppUser.Role.ADMIN,c1);
    user(u,e,"usuario@norte.coop","Usuario Norte",AppUser.Role.USER,c1);
    user(u,e,"admin@sur.coop","Admin Sur",AppUser.Role.ADMIN,c2);
    t.save(new DocType(null,"Acta de Federación",true));
    for(String n:new String[]{"Acta de Asamblea","Estatutos","Balance financiero","Registro de socios","Certificación legal"}) t.save(new DocType(null,n,false));
  };}
  private void user(UserRepo u,PasswordEncoder e,String mail,String name,AppUser.Role r,Organization org){
    var x=new AppUser(); x.setEmail(mail); x.setFullName(name); x.setRole(r); x.setOrg(org); x.setPasswordHash(e.encode("demo1234")); u.save(x); }
}
