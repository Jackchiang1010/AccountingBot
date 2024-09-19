package com.example.accountbot.service;

import com.example.accountbot.dto.alert.AlertDto;
import com.example.accountbot.dto.category.CategoryDto;

import java.util.Map;

public interface AlertService {

    Map<String, Object> create(AlertDto alertDto);

    Map<String, Object> get(String lineUserId);

}
