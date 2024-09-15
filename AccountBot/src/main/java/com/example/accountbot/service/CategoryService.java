package com.example.accountbot.service;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.transaction.TransactionDto;

import java.util.Map;

public interface CategoryService {

    Map<String, Object> create(CategoryDto categoryDto);

}
