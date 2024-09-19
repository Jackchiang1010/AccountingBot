package com.example.accountbot.dto.alert;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetAlertDto {

    private String time;

    private String description;

}
