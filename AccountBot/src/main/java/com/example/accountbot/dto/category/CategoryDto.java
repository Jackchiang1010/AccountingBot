package com.example.accountbot.dto.category;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryDto {

    private Integer id;

    private Integer type;

    private String name;

    private String lineUserId;

}
