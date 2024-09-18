package com.example.accountbot.dto.budget;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BudgetDto {

    private String category;

    private Integer price;

//    @JsonProperty("lineuser_id")
    private String lineUserId;

}
