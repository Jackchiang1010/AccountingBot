package com.example.accountbot.repository;

import com.example.accountbot.dto.TransactionDto;

public interface TransactionRepository {

    Integer recordTransaction(TransactionDto transactionDto);
    Integer getCategoryId(String categoryName, String lineUserId);
}
