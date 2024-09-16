package com.example.accountbot.repository;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;

import java.util.List;

public interface CategoryRepository {

    Integer create(CategoryDto categoryDto);

    List<CategoryDto> get(Integer type, String name, String lineUserId);

    void initializeDefaultCategories(String lineUserId);

    UpdateCategoryDto update(UpdateCategoryDto updateCategoryDto);

    boolean checkCategoryNameExists(String lineUserId, String name);

    boolean delete(Integer id);

}
