package com.example.accountbot.dto.budget;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateBudgetResponseDto {

    private Integer id;

    private String category;

    private Integer price;

}
