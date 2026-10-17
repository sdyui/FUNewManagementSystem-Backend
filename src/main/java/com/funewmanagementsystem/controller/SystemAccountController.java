package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.service.SystemAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class SystemAccountController {
    private final SystemAccountService systemAccountService;
    @PostMapping("/auth/login")
    public ResponseEntity<SystemAccountResponse> login(@Valid @RequestBody SystemAccountRequest systemAccountRequest){
        SystemAccountResponse systemAccountResponse = systemAccountService.login(systemAccountRequest);
        return ResponseEntity.ok(systemAccountResponse);
    }
}
