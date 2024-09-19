package com.example.accountbot.dto.transaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetAllTransactionDto {

    private Integer type;

    private String category;

    private Integer cost;

    private String description;

    private String date;

}
