package com.funewmanagementsystem.controller;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.funewmanagementsystem.dto.request.CreateAccountRequest;
import com.funewmanagementsystem.dto.request.SystemAccountRequest;
import com.funewmanagementsystem.dto.request.UpdateAccount;
import com.funewmanagementsystem.dto.respone.PageResponse;
import com.funewmanagementsystem.dto.respone.SystemAccountResponse;
import com.funewmanagementsystem.entity.SystemAccount;
import com.funewmanagementsystem.exception.ErrorResponse;
import com.funewmanagementsystem.exception.ResourceNotFoundException;
import com.funewmanagementsystem.service.SystemAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SystemAccountController {
    private static final String USER_EXAMPLE = """
            {
              "accountId": 1,
              "accountName": "admin",
              "accountEmail": "admin@gmai.com",
              "accountRole": "ADMIN"
            }""";
    private static final String PAGE_EXAMPLE = """
            {
              "content": [""" + USER_EXAMPLE + """
            ],
              "page": 0,
              "size": 10,
              "totalElements": 1,
              "totalPages": 1,
              "last": true
            }""";
    private static final String BAD_SORT_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Cannot sort by 'accountPassword'. Allowed: [id, accountName, accountEmail, accountRole]",
              "path": "/api/v1/account"
            }""";
    private static final String DELETE_CONFLICT_EXAMPLE = """
            {
              "timestamp": "2026-10-17T22:30:00",
              "status": 409,
              "error": "Conflict",
              "message": "Operation conflicts with existing data",
              "path": "/api/v1/account/1"
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
            summary = "Search accounts",
            description = "Returns a page of accounts, optionally filtered by name and email (partial, case-insensitive). "
                    + "Sort with sort=field,asc|desc; allowed fields: id, accountName, accountEmail, accountRole.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of accounts (content is empty when nothing matches)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponse.class),
                            examples = @ExampleObject(name = "page", value = PAGE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Invalid sort field",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "badSort", value = BAD_SORT_EXAMPLE)))
    })
    @GetMapping("/account")
    public ResponseEntity<PageResponse<SystemAccountResponse>> fetchAllAccount(
            @Parameter(description = "Part of account name", example = "adm")
            @RequestParam(required = false) String name,
            @Parameter(description = "Part of email", example = "gmail")
            @RequestParam(required = false) String email,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(systemAccountService.searchAccounts(name, email, pageable));
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
    public ResponseEntity<SystemAccountResponse> fetchAccountById(@Parameter(description = "Id of the account to retrieve", example = "1", required = true)
                                              @PathVariable long id){
        return ResponseEntity.ok(systemAccountService.getAccountById(id));
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
    @Operation(
            summary = "Delete an account",
            description = "Deletes the account with the given id. Fails with 409 if the account still owns news articles.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "notFound", value = NOT_FOUND_EXAMPLE))),
            @ApiResponse(responseCode = "409", description = "Account is still referenced by news articles",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "conflict", value = DELETE_CONFLICT_EXAMPLE)))
    })
    @DeleteMapping("/account/{id}")
    public ResponseEntity<Void> deleteAccount(@Parameter(description = "Id of the account to delete",example = "1", required = true)
                                           @PathVariable long id
                                           ){
        systemAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/account/{id}")
    public ResponseEntity<?>updateSystemAccount(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateAccount updateAccount){
        return ResponseEntity.ok(systemAccountService.save(id,updateAccount));
    }





}
