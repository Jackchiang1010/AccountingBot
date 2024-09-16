package com.example.accountbot.controller;

import com.example.accountbot.dto.ErrorResponseDto;
import com.example.accountbot.dto.transaction.BalanceDto;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/1.0/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/record")
    public ResponseEntity<?> record(@RequestBody TransactionDto transactionDto) {

        try {
            Map<String, Object> response = transactionService.record(transactionDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get")
    public ResponseEntity<?> getTransaction(@RequestParam(value = "type") Integer type,
                                            @RequestParam(value = "category") String category,
                                            @RequestParam(value = "time") String time,
                                            @RequestParam(value = "lineUserId") String lineUserId) {

        try {
            Map<String, Object> response = transactionService.getTransaction(type, category, time, lineUserId);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateTransactionDto updatetransactionDto) {

        try {
            Map<String, Object> response = transactionService.update(updatetransactionDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody UpdateTransactionDto updatetransactionDto) {

        try {
            boolean result = transactionService.delete(updatetransactionDto.getId());

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

    @GetMapping("/balance")
    public ResponseEntity<?> balance() {

        try {
            BalanceDto response = transactionService.balance();

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
