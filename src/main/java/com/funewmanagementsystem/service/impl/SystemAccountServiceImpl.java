package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.AccountRole;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.DuplicateResourceException;
import com.funewmanagementsystem.exception.InvalidCredentialsException;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import com.funewmanagementsystem.service.SystemAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class SystemAccountServiceImpl implements SystemAccountService {
    private final SystemAccountRepository systemAccountRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public SystemAccountResponse login(SystemAccountRequest systemAccountRequest) {
        SystemAccount systemAccount = systemAccountRepository.
                findByAccountName(systemAccountRequest.getAccountName()).orElseThrow(
                        () -> new InvalidCredentialsException("Invalid Account Name or Password")
                );
        if(!passwordEncoder.matches(systemAccountRequest.getAccountPassword(),systemAccount.getAccountPassword())){
            throw new InvalidCredentialsException("Invalid Account Name or Password");
        }
        return new SystemAccountResponse(systemAccount.getId(),systemAccount.getAccountName(),systemAccount.getAccountRole());
    }

    @Override
    public SystemAccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        if(systemAccountRepository.existsByAccountName(createAccountRequest.getAccountName())){
            throw new DuplicateResourceException("Account name already exists");
        }
        if(systemAccountRepository.existsByAccountEmail(createAccountRequest.getAccountEmail())){
            throw new DuplicateResourceException("Email already exists");
        }


        SystemAccount account = new SystemAccount();
        account.setAccountName(createAccountRequest.getAccountName());
        account.setAccountEmail(createAccountRequest.getAccountEmail());
        account.setAccountPassword(passwordEncoder.encode(createAccountRequest.getAccountPassword()));
        account.setAccountRole(AccountRole.STAFF);

        SystemAccount saved = systemAccountRepository.save(account);
        return new SystemAccountResponse(saved.getId(),saved.getAccountName(),saved.getAccountRole());
    }



}
