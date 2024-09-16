package com.example.accountbot.service;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.category.CategoryDto;

import java.util.Map;

public interface BudgetService {

    Map<String, Object> create(BudgetDto budgetDto);

    Map<String, Object> get(String categoryId, String lineUserId);

}
