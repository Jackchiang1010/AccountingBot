package com.example.accountbot.repository;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.budget.GetBudgetDto;

import java.util.List;

public interface BudgetRepository {

    Integer create(BudgetDto budgetDto);

    Integer getCategoryId(String categoryName, String lineUserId);

    List<GetBudgetDto> get(String category, String lineUserId);

}
