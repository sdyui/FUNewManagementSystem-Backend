package com.funewmanagementsystem.dto.request;

import com.funewmanagementsystem.entity.AccountRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemAccountRequest {
    @NotBlank(message = "Username cannot be empty")
    private String accountName;
    @NotBlank(message = "Password cannot be empty")
    private String accountPassword;

}
