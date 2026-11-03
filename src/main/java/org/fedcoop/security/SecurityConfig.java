package org.fedcoop.security;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.List;
import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
@Configuration
public class SecurityConfig {
  @Bean PasswordEncoder encoder(){ return new BCryptPasswordEncoder(); }
  @Bean SecurityFilterChain chain(HttpSecurity http, JwtService jwt) throws Exception {
    http    .headers(headers -> headers.frameOptions(frame -> frame.disable())) //para ver h2-console
            .csrf(c->c.disable())
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .cors(c->{})
      .authorizeHttpRequests(a->a.requestMatchers("/api/auth/**").permitAll().requestMatchers("/api/**").authenticated().anyRequest().permitAll())
      .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->r.sendError(401)))
      .addFilterBefore(new OncePerRequestFilter(){
        protected void doFilterInternal(HttpServletRequest rq, HttpServletResponse rs, FilterChain ch) throws ServletException, IOException {
          String h = rq.getHeader("Authorization");
          if (h!=null && h.startsWith("Bearer ")) try {
            Ctx c = jwt.parse(h.substring(7));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(c,null,List.of()));
          } catch (Exception ignored) {}
          ch.doFilter(rq,rs);
        }}, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
