package com.example.accountbot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransactionDto {

    private Integer type;

    private String category;

    private Integer cost;

    private String description;

    private String date;

    private String lineUserId;

}
