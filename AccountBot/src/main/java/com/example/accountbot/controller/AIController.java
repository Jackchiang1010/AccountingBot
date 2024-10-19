package com.example.accountbot.controller;

import com.example.accountbot.dto.ErrorResponseDto;
import com.example.accountbot.service.AIService;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/1.0/AI")
public class AIController {

    private final AIService aiService;

    private final TransactionService transactionService;

    public static final int EXPENSE_CATEGORY = 1;
    public static final int INCOME_CATEGORY = 0;

    @GetMapping("/feedback")
    public ResponseEntity<?> getFeedback(@RequestParam(value = "lineUserId") String lineUserId) {

        try {

            Map<String, Object> expense = transactionService.getTransaction(EXPENSE_CATEGORY, "all", "lastMonth", lineUserId);
            Map<String, Object> income = transactionService.getTransaction(INCOME_CATEGORY, "all", "lastMonth", lineUserId);

            Map<String, Object> response = aiService.getFeedback(expense, income);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
