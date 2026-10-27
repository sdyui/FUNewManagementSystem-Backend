package com.funewmanagementsystem.security;

import com.funewmanagementsystem.entity.AccountRole;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.InvalidCredentialsException;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final SystemAccountRepository systemAccountRepository;

    public SystemAccount getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new InvalidCredentialsException("You must login first");
        }
        return systemAccountRepository.findByAccountName(auth.getName())
                .orElseThrow(() -> new InvalidCredentialsException("Account is not exists"));
    }

    public boolean isReviewer(SystemAccount account) {
        return account.getAccountRole() == AccountRole.EDITOR
                || account.getAccountRole() == AccountRole.ADMIN;
    }

    public boolean isAdmin(SystemAccount account) {
        return account.getAccountRole() == AccountRole.ADMIN;
    }
}