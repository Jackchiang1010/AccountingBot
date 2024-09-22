package com.example.accountbot.controller;

import com.example.accountbot.dto.ErrorResponseDto;
import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/1.0/budget")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody BudgetDto budgetDto) {

        try {
            Map<String, Object> response = budgetService.create(budgetDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get")
    public ResponseEntity<?> get(@RequestParam(value = "category", defaultValue = "all") String categoryId,
                                 @RequestParam(value = "lineUserId") String lineUserId) {

        try {
            Map<String, Object> response = budgetService.get(categoryId, lineUserId);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateBudgetDto updateBudgetDto) {

        try {
            Map<String, Object> response = budgetService.update(updateBudgetDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody UpdateBudgetDto updateBudgetDto) {

        try {
            boolean result = budgetService.delete(updateBudgetDto.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("result", result);

            if(result == true) {
                return ResponseEntity.ok(response);
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
