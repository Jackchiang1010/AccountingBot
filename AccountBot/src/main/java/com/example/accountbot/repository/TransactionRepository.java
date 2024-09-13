package com.example.accountbot.repository;

import com.example.accountbot.dto.CategoryCostDto;
import com.example.accountbot.dto.GetTransactionDto;
import com.example.accountbot.dto.TransactionDto;

import java.util.List;

public interface TransactionRepository {

    Integer recordTransaction(TransactionDto transactionDto);
    Integer getCategoryId(String categoryName, String lineUserId);

    List<CategoryCostDto> getTransaction(Integer type, String category, String startDate, String endDate);

}
