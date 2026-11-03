package org.fedcoop.domain;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor
public class AppUser {
  public enum Role { ADMIN, USER }
  @Id @GeneratedValue Long id;
  @Column(unique=true, nullable=false) String email;
  String fullName, passwordHash;
  @Enumerated(EnumType.STRING) Role role;
  @Column(columnDefinition="boolean default true") boolean active = true;
  @ManyToOne(optional=false) Organization org;   // tenant del usuario
}
