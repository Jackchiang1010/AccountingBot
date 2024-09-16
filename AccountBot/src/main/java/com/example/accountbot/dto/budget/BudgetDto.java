package com.example.accountbot.dto.budget;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BudgetDto {

    private String category;

    private String price;

    private String lineUserId;

}
