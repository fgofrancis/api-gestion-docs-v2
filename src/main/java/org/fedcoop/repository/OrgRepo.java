package org.fedcoop.repository;

import org.fedcoop.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgRepo extends JpaRepository<Organization, Long> {
    List<Organization> findByKind(Organization.Kind k);
}
