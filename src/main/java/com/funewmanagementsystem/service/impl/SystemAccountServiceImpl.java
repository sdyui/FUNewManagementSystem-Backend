package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import com.funewmanagementsystem.service.SystemAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
public class SystemAccountServiceImpl implements SystemAccountService {
    private final SystemAccountRepository systemAccountRepository;
    private final   PasswordEncoder passwordEncoder;
    @Override
    public SystemAccountResponse login(SystemAccountRequest systemAccountRequest) {
        SystemAccount systemAccount = systemAccountRepository.
                findByAccountName(systemAccountRequest.getAccountName()).orElseThrow(
                        ()->
                                new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,"Invalid Account Name or Password"

                        )

                );
        if(!passwordEncoder.matches(systemAccountRequest.getAccountPassword(),systemAccount.getAccountPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid Account Name or Account Password");
        }
        return new SystemAccountResponse(systemAccount.getId(),systemAccount.getAccountName(),systemAccount.getAccountRole());
    }


}
