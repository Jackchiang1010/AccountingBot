package com.example.accountbot.dto.alert;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlertDto {

    private String time;

    private String description;

    private String lineUserId;

}
