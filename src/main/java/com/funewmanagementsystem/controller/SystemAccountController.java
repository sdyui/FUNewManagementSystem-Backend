package com.funewmanagementsystem.controller;

import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.ErrorResponse;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.repository.SystemAccountRepository;
import com.funewmanagementsystem.service.SystemAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SystemAccountController {
    private SystemAccountResponse toResponse(SystemAccount a) {
        SystemAccountResponse res = new SystemAccountResponse();
        res.setAccountId(a.getId());
        res.setAccountName(a.getAccountName());
        res.setAccountRole(a.getAccountRole());
        return res;
    }
    private static final String USER_EXAMPLE = """
            {
              "accountId": 1,
              "accountName": "admin",
              "accountRole": "ADMIN"
            }""";
    private static final String LOGIN_REQUEST_EXAMPLE = """
            {
              "accountName": "admin",
              "accountPassword": "123456"
            }""";
    private static final String LOGIN_VALIDATION_ERROR_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Validation failed",
              "path": "/api/v1/auth/login",
              "errors": {
                "accountPassword": "Password cannot be empty"
              }
            }""";
    private static final String LOGIN_UNAUTHORIZED_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 401,
              "error": "Unauthorized",
              "message": "Invalid Account Name or Password",
              "path": "/api/v1/auth/login"
            }""";
    private static final String CREATE_ACCOUNT_REQUEST_EXAMPLE = """
            {
              "accountName": "staff01",
              "accountEmail": "staff01@funews.com",
              "accountPassword": "123456"
            }""";
    private static final String CREATED_USER_EXAMPLE = """
            {
              "accountId": 3,
              "accountName": "staff01",
              "accountRole": "STAFF"
            }""";
    private static final String NOT_FOUND_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Account with id 99 not found",
              "path": "/api/v1/account/99"
            }""";
    private static final String VALIDATION_ERROR_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Validation failed",
              "path": "/api/v1/account",
              "errors": {
                "accountEmail": "Invalid mail example: admin@funews.com",
                "accountPassword": "Password must be at least 6 characters"
              }
            }""";
    private static final String DUPLICATE_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 409,
              "error": "Conflict",
              "message": "Email already exists",
              "path": "/api/v1/account"
            }""";
    private final SystemAccountService systemAccountService;
    private final SystemAccountRepository systemAccountRepository;
    @Operation(
            summary = "Login",
            description = "Authenticate a user using account name and password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SystemAccountRequest.class),
                            examples = @ExampleObject(name = "login", value = LOGIN_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SystemAccountResponse.class),
                            examples = @ExampleObject(name = "account", value = USER_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (missing account name or password)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "validationError", value = LOGIN_VALIDATION_ERROR_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "Invalid account name or password",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "unauthorized", value = LOGIN_UNAUTHORIZED_EXAMPLE)))
    })
    @PostMapping("/auth/login")
    public ResponseEntity<SystemAccountResponse> login(@Valid @RequestBody SystemAccountRequest systemAccountRequest){
        SystemAccountResponse systemAccountResponse = systemAccountService.login(systemAccountRequest);
        return ResponseEntity.ok(systemAccountResponse);
    }
    @Operation(
            summary = "Get all accounts",
            description = "Returns every account stored in the database. Returns 204 No Content when there are no accounts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of accounts",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SystemAccountResponse.class)),
                            examples = @ExampleObject(name = "accounts", value = "[" + USER_EXAMPLE + "]"))),
            @ApiResponse(responseCode = "204", description = "No accounts exist", content = @Content)
    })
    @GetMapping("/account")
    public ResponseEntity<List<SystemAccountResponse>> fetchAllAccount(){
        List<SystemAccountResponse> accounts = systemAccountRepository.findAll().stream().map(this::toResponse).toList();
        if(accounts.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(accounts);
    }
    @Operation(
            summary = "Get an account by id",
            description = "Returns the account with the given id, or 404 Not Found if no such account exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SystemAccountResponse.class),
                            examples = @ExampleObject(name = "account", value = USER_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound", value = NOT_FOUND_EXAMPLE)))
    })

    @GetMapping("/account/{id}")
    public ResponseEntity<?> fetchAccountById(@Parameter(description = "Id of the account to retrieve", example = "1", required = true)
                                              @PathVariable long id){
        SystemAccountResponse account = systemAccountRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        return ResponseEntity.ok(account);
    }
    @Operation(
            summary = "Create a new account",
            description = "Creates a new account with role STAFF. The password is hashed with BCrypt before saving. "
                    + "Account name and email must be unique.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateAccountRequest.class),
                            examples = @ExampleObject(name = "newAccount", value = CREATE_ACCOUNT_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SystemAccountResponse.class),
                            examples = @ExampleObject(name = "created", value = CREATED_USER_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (missing field, wrong email format, password too short)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "validationError", value = VALIDATION_ERROR_EXAMPLE))),
            @ApiResponse(responseCode = "409", description = "Account name or email already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "duplicate", value = DUPLICATE_EXAMPLE)))
    })
    @PostMapping("/account")
    public ResponseEntity<SystemAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest){
        SystemAccountResponse created = systemAccountService.createAccount(createAccountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @DeleteMapping("/account/{id}")
    public ResponseEntity<?> deleteAccount(@Parameter(description = "Id of the account to delete",example = "1", required = true)
                                           @PathVariable long id
                                           ){
        Optional<SystemAccount> account = systemAccountRepository.findById(id);
        if(!account.isPresent()){
            throw new ResourceNotFoundException("Account with id " + id + " not found");
        }
        systemAccountRepository.deleteById(id);
        return new ResponseEntity<>("Account with id " + id + " deleted", HttpStatus.OK);
    }



}
