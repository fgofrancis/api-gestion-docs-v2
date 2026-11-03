package org.fedcoop.repository;

import org.fedcoop.domain.Doc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocRepo extends JpaRepository<Doc, Long> {
    @Query("select d from Doc d where (:org is null or d.org.id=:org) and (:type is null or d.type.id=:type) and lower(d.title) like lower(concat('%',:q,'%'))")
    Page<Doc> search(@Param("org") Long org, @Param("type") Long type, @Param("q") String q, Pageable p);
}
