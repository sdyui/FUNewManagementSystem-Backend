package com.funewmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemAccountRequest {
    @NotBlank(message = "Username cannot be empty")
    private String accountName;
    @NotBlank(message = "Password cannot be empty")
    private String accountPassword;

}
