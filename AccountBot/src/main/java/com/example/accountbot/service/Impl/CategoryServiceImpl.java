package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.repository.CategoryRepository;
import com.example.accountbot.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Map<String, Object> create(CategoryDto categoryDto) {
        Integer categoryId = categoryRepository.create(categoryDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", categoryId);

        return result;
    }

    @Override
    public Map<String, Object> get(Integer type, String name, String lineUserId) {
        List<CategoryDto> getCategoryDto = categoryRepository.get(type, name, lineUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", getCategoryDto);

        return result;
    }

    @Override
    public Map<String, Object> update(UpdateCategoryDto updateCategoryDto) {
        UpdateCategoryDto updatedCategoryDto = categoryRepository.update(updateCategoryDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", updatedCategoryDto);

        return result;
    }

    @Override
    public boolean delete(Integer id) {
        return categoryRepository.delete(id);
    }
}
