package com.funewmanagementsystem.dto.respone;

import com.funewmanagementsystem.entity.AccountRole;
import com.funewmanagementsystem.entity.SystemAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemAccountResponse {

    private Long accountId;
    private String accountName;
    private AccountRole accountRole;


}