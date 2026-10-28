package com.funewmanagementsystem.security;

import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final SystemAccountRepository  systemAccountRepository;
    @Override
    public UserDetails loadUserByUsername(String username)  {
        SystemAccount a = systemAccountRepository.findByAccountName(username)
                .orElseThrow(()->new UsernameNotFoundException(username));
        return User.withUsername(a.getAccountName())
                .password(a.getAccountPassword())
                .roles(a.getAccountRole().name()).build();
    }
}
