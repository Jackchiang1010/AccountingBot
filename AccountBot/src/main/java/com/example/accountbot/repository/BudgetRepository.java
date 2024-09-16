package com.example.accountbot.repository;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.category.CategoryDto;

public interface BudgetRepository {

    Integer create(BudgetDto budgetDto);

}
