package com.example.accountbot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetTransactionDto {

    private String startDate;

    private String endDate;

    private List<CategoryDto> category;

    private List<Integer> cost;

}
