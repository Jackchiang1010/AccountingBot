package com.example.accountbot.service;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;

import java.util.Map;

public interface CategoryService {

    Map<String, Object> create(CategoryDto categoryDto);

    Map<String, Object> get(Integer type, String name, String lineUserId);

    Map<String, Object> update(UpdateCategoryDto UpdateCategoryDto);

    boolean delete(Integer id);

}
