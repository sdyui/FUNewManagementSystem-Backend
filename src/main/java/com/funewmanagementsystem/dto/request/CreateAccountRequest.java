package com.funewmanagementsystem.dto.request;

import com.funewmanagementsystem.entity.AccountRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotBlank(message = "Username cannot be empty")
    private String accountName;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid mail example: admin@funews.com")
    private String accountEmail;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String accountPassword;
    @NotBlank(message = "Comfirm password can not be empty")
    @Size(min = 6, message = "Comfirm password must be at lease 6 characters")
    private String comfirmAccountPassword;

}