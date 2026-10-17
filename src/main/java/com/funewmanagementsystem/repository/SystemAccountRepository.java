package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAccountRepository extends JpaRepository<SystemAccount,Long> {

    Optional<SystemAccount> findByAccountName(String accountName);

    boolean existsByAccountName(String accountName);
    boolean existsByAccountEmail(String accountEmail);
}
