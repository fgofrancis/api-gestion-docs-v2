package org.fedcoop.domain;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor
public class Doc {
  @Id @GeneratedValue Long id;
  String title; @Column(length=1000) String description;
  LocalDate docDate; Instant createdAt = Instant.now();
  String fileName, contentType, storedName, uploadedBy; long size;
  @ManyToOne(optional=false) DocType type;
  @ManyToOne(optional=false) Organization org;   // tenant propietario
}
