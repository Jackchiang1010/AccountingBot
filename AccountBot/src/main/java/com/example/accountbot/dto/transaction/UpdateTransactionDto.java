package com.example.accountbot.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTransactionDto {

    private Integer id;

    private Integer type;

    private String category;

    private Integer cost;

    private String description;

    private String date;

    private String lineUserId;

}
