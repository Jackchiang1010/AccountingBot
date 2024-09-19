package com.example.accountbot.repository;

import com.example.accountbot.dto.alert.AlertDto;

public interface AlertRepository {

    Integer create(AlertDto alertDto);

}
