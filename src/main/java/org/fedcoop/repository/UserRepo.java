package org.fedcoop.repository;

import org.fedcoop.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String e);
    List<AppUser> findByOrgIdOrderByFullName(Long orgId);
    List<AppUser> findAllByOrderByFullName();
    long countByOrgId(Long orgId);
}
