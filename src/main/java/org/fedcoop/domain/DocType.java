package org.fedcoop.domain;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocType {
  @Id @GeneratedValue Long id;
  String name;
  /** true = solo la Federación puede registrarlo (ej. actas propias) */
  boolean federationOnly;
}
