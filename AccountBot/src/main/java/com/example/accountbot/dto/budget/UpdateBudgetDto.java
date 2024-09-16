package com.example.accountbot.dto.budget;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateBudgetDto {

    private Integer id;

    private String category;

    private Integer price;

    private String lineUserId;

}
