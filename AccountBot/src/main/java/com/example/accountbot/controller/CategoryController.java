package com.example.accountbot.controller;

import com.example.accountbot.dto.ErrorResponseDto;
import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;
import com.example.accountbot.service.CategoryService;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/1.0/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CategoryDto categoryDto) {

        try {
            Map<String, Object> response = categoryService.create(categoryDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get")
    public ResponseEntity<?> get(@RequestParam(value = "type") Integer type,
                                 @RequestParam(value = "name",defaultValue = "all") String name,
                                 @RequestParam(value = "lineUserId") String lineUserId) {

        try {
            Map<String, Object> response = categoryService.get(type, name, lineUserId);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateCategoryDto updateCategoryDto) {

        try {
            Map<String, Object> response = categoryService.update(updateCategoryDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
