package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.request.UpdateAccount;
import com.funewmanagementsystem.dto.respone.LoginResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.SystemAccount;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SystemAccountService {
   LoginResponse login(SystemAccountRequest systemAccountRequest);
   SystemAccountResponse createAccount(CreateAccountRequest createAccountRequest);
   PageResponse<SystemAccountResponse> searchAccounts(String name, String email, Pageable pageable);
   SystemAccountResponse getAccountById(Long id);
   void deleteAccount(Long id);
   SystemAccountResponse save(Long id,UpdateAccount updateAccount);
   Optional<SystemAccount> findById(Long id);
}
