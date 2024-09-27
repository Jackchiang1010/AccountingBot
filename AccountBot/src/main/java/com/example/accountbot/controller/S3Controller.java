package com.example.accountbot.controller;

import com.example.accountbot.dto.ErrorResponseDto;
import com.example.accountbot.service.S3Service;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/1.0/S3")
public class S3Controller {

    private final S3Service s3Service;

    private final TransactionService transactionService;

    @GetMapping("/export")
    public ResponseEntity<?> export(@RequestParam(value = "startDate") String startDate,
                                 @RequestParam(value = "endDate") String endDate,
                                 @RequestParam(value = "lineUserId") String lineUserId) {

        try {
            String response = s3Service.exportCsvFile(lineUserId, transactionService.getAllTransaction(startDate, endDate, lineUserId));

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
