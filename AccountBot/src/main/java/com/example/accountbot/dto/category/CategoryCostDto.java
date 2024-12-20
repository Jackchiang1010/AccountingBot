package com.example.accountbot.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryCostDto {

    private String category;

    @JsonProperty("total_cost")
    private Integer totalCost;

}
