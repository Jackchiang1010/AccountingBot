package com.example.accountbot.service;

import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;

import java.time.LocalDate;
import java.util.Map;

public interface TransactionService {

    Map<String, Object> record(TransactionDto transactionDto);

    Map<String, Object> getTransaction(Integer type, String category, String time);
    void createTransaction(Integer type, String category, Integer cost, String description, String date, String lineUserId);
    String dateToString(LocalDate date);

    Map<String, Object> update(UpdateTransactionDto updatetransactionDto);

    boolean delete(Integer id);

}
