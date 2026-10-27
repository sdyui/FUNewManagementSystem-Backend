package com.funewmanagementsystem.service.impl;

import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.request.UpdateAccount;
import com.funewmanagementsystem.dto.respone.LoginResponse;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.AccountRole;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.DuplicateResourceException;
import com.funewmanagementsystem.exception.InvalidCredentialsException;
import com.funewmanagementsystem.exception.InvalidRequestException;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import com.funewmanagementsystem.security.JwtService;
import com.funewmanagementsystem.service.SystemAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SystemAccountServiceImpl implements SystemAccountService {
    private final SystemAccountRepository systemAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Override
    public LoginResponse login(SystemAccountRequest systemAccountRequest) {
        SystemAccount systemAccount = systemAccountRepository.
                findByAccountName(systemAccountRequest.getAccountName()).orElseThrow(
                        () -> new InvalidCredentialsException("Invalid Account Name or Password")
                );
        if(!passwordEncoder.matches(systemAccountRequest.getAccountPassword(),systemAccount.getAccountPassword())){
            throw new InvalidCredentialsException("Invalid Account Name or Password");
        }
        SystemAccountResponse account = new SystemAccountResponse(systemAccount.getId(),systemAccount.getAccountName(),systemAccount.getAccountEmail(),systemAccount.getAccountRole());
        return new LoginResponse(jwtService.generateToken(systemAccount), "Bearer", account);
    }

    @Override
    public SystemAccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        if(systemAccountRepository.existsByAccountName(createAccountRequest.getAccountName())){
            throw new DuplicateResourceException("Account name already exists");
        }
        if(systemAccountRepository.existsByAccountEmail(createAccountRequest.getAccountEmail())){
            throw new DuplicateResourceException("Email already exists");
        }
        if(!createAccountRequest.getAccountPassword().equals(createAccountRequest.getComfirmAccountPassword())){
            throw new InvalidRequestException("comfirm password and password was wrongs");
        }



        SystemAccount account = new SystemAccount();
        account.setAccountName(createAccountRequest.getAccountName());
        account.setAccountEmail(createAccountRequest.getAccountEmail());
        account.setAccountPassword(passwordEncoder.encode(createAccountRequest.getAccountPassword()));
        account.setAccountRole(AccountRole.STAFF);

        SystemAccount saved = systemAccountRepository.save(account);
        return new SystemAccountResponse(saved.getId(),saved.getAccountName(),saved.getAccountEmail(),saved.getAccountRole());
    }

    @Override
    public PageResponse<SystemAccountResponse> searchAccounts(String name, String email, Pageable pageable) {
        for(Sort.Order order : pageable.getSort()){
            if(!SORTABLE_FIELDS.contains(order.getProperty())){
                throw new InvalidRequestException("Cannot sort by '"+ order.getProperty() +"'. Allowed: "+SORTABLE_FIELDS);
            }
        }
        Page<SystemAccountResponse> result = systemAccountRepository.
                search(blankToNull(name),blankToNull(email),pageable)
                .map(a -> new SystemAccountResponse(a.getId(), a.getAccountName(),a.getAccountEmail(), a.getAccountRole()));
    return PageResponse.of(result);
    }

    @Override
    public SystemAccountResponse getAccountById(Long id) {
        SystemAccount a = systemAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        return new SystemAccountResponse(a.getId(), a.getAccountName(),a.getAccountEmail(), a.getAccountRole());
    }

    @Override
    public void deleteAccount(Long id) {
        if(!systemAccountRepository.existsById(id)){
            throw new ResourceNotFoundException("Account with id " + id + " not found");
        }
        systemAccountRepository.deleteById(id);
        systemAccountRepository.flush();
    }
    @Transactional
    @Override
    public SystemAccountResponse save(Long id,UpdateAccount updateAccount) {
    SystemAccount account = systemAccountRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("not found id"));
    if(systemAccountRepository.existsByAccountEmailAndIdNot(updateAccount.getAccountEmail(),id)){
        throw new DuplicateResourceException("Email already exist");
    }
        account.setAccountEmail(updateAccount.getAccountEmail());

        SystemAccount s1 = systemAccountRepository.save(account);
        return new SystemAccountResponse(s1.getId(),s1.getAccountName(),s1.getAccountEmail(),s1.getAccountRole());
    }

    @Override
    public Optional<SystemAccount> findById(Long id) {
        return systemAccountRepository.findById(id);
    }

    private static final Set<String> SORTABLE_FIELDS = Set.of("id","accountName","accountEmail","accountRole");
    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }



}
