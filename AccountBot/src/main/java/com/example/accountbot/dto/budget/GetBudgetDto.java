package com.example.accountbot.dto.budget;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@JsonPropertyOrder({"category", "price"})
public class GetBudgetDto {

    private String category;

    private Integer price;

}
