package org.fedcoop.web;
import org.springframework.context.annotation.*; import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class CorsConfig implements WebMvcConfigurer {
  public void addCorsMappings(CorsRegistry r){
    r.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200","https://eloquent-zuccutto-5c795c.netlify.app")
            .allowedMethods("*").allowedHeaders("*").exposedHeaders("Content-Disposition"); }
}
