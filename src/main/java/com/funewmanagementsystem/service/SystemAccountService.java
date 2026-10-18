package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import org.springframework.data.domain.Pageable;

public interface SystemAccountService {
   SystemAccountResponse login(SystemAccountRequest systemAccountRequest);
   SystemAccountResponse createAccount(CreateAccountRequest createAccountRequest);
   PageResponse<SystemAccountResponse> searchAccounts(String name, String email, Pageable pageable);
   SystemAccountResponse getAccountById(Long id);
   void deleteAccount(Long id);
}
