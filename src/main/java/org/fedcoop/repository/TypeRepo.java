package org.fedcoop.repository;

import org.fedcoop.domain.DocType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepo extends JpaRepository<DocType, Long> {
}
