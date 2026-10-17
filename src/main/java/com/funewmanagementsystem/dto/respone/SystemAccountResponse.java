package com.funewmanagementsystem.dto.respone;

import com.funewmanagementsystem.entity.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemAccountResponse {

    private Long accountId;
    private String accountName;
    private AccountRole accountRole;
}