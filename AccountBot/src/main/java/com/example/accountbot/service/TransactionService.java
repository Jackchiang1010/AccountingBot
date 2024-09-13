package com.example.accountbot.service;

import com.example.accountbot.dto.TransactionDto;

import java.util.Map;

public interface TransactionService {

    Map<String, Object> record(TransactionDto transactionDto);
    Map<String, Object> getTransaction(Integer type, String category, String time);

}
