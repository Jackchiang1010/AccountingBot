package com.example.accountbot.repository;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.budget.GetBudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetResponseDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;

import java.util.List;

public interface BudgetRepository {

    Integer create(BudgetDto budgetDto);

    Integer getCategoryId(String categoryName, String lineUserId);

    List<GetBudgetDto> get(String category, String lineUserId);

    UpdateBudgetResponseDto update(UpdateBudgetDto updateBudgetDto);

    boolean delete(Integer id);

}
