package com.funewmanagementsystem.repository;

import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.entity.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAccountRepository extends JpaRepository<SystemAccount,Integer> {

    Optional<SystemAccount> findByAccountName(String accountName);
}
