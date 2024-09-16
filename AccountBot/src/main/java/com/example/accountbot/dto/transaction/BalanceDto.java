package com.example.accountbot.dto.transaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BalanceDto {

    private Integer totalIncome;
    private Integer totalExpenses;

}
