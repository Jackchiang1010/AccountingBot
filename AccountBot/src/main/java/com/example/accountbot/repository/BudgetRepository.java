package com.example.accountbot.repository;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.category.CategoryDto;

import java.util.List;

public interface BudgetRepository {

    Integer create(BudgetDto budgetDto);

    Integer getCategoryId(String categoryName, String lineUserId);

    List<BudgetDto> get(String category, String lineUserId);

}
