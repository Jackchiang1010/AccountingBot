package com.example.accountbot.repository;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.transaction.TransactionDto;

import java.util.List;

public interface CategoryRepository {

    Integer create(CategoryDto categoryDto);

    List<CategoryDto> get(Integer type, String name, String lineUserId);

}
