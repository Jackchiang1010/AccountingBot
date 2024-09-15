package com.example.accountbot.repository;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.transaction.TransactionDto;

public interface CategoryRepository {

    Integer create(CategoryDto categoryDto);

}
