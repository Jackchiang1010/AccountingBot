package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.budget.GetBudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetResponseDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.repository.BudgetRepository;
import com.example.accountbot.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    @Override
    public Map<String, Object> create(BudgetDto budgetDto) {

        Integer budgetId = budgetRepository.create(budgetDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", budgetId);

        return result;
    }

    @Override
    public Map<String, Object> get(String category, String lineUserId) {
        List<GetBudgetDto> getCategoryDto = budgetRepository.get(category, lineUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", getCategoryDto);

        return result;
    }

    @Override
    public Map<String, Object> update(UpdateBudgetDto updateBudgetDto) {

        UpdateBudgetResponseDto updatedBudgetResponseDto = budgetRepository.update(updateBudgetDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", updatedBudgetResponseDto);

        return result;
    }

    @Override
    public boolean delete(Integer id) {
        return budgetRepository.delete(id);
    }
}
