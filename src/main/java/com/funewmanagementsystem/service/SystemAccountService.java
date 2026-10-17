package com.funewmanagementsystem.service;

import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;

public interface SystemAccountService {
   SystemAccountResponse login(SystemAccountRequest systemAccountRequest);
   SystemAccountResponse createAccount(CreateAccountRequest createAccountRequest);

}
