package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.entity.SystemAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAccountRepository extends JpaRepository<SystemAccount,Long> {
    Optional<SystemAccount> findByAccountName(String accountName);
    boolean existsByAccountName(String accountName);
    boolean existsByAccountEmail(String accountEmail);
    @Query("""
        select a from SystemAccount a
        where (:name  is null or lower(a.accountName)  like lower(concat('%', :name,  '%')))
          and (:email is null or lower(a.accountEmail) like lower(concat('%', :email, '%')))
        """)
    Page<SystemAccount> search(@Param("name") String name,
                               @Param("email") String email,
                               Pageable pageable);

}
