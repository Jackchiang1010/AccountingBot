package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.repository.CategoryRepository;
import com.example.accountbot.repository.TransactionRepository;
import com.example.accountbot.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Map<String, Object> create(CategoryDto categoryDto) {
        Integer transactionId = categoryRepository.create(categoryDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", transactionId);

        return result;
    }
}
