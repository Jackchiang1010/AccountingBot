package com.example.accountbot.repository;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;

import java.util.List;

public interface TransactionRepository {

    Integer recordTransaction(TransactionDto transactionDto);
    Integer getCategoryId(String categoryName, String lineUserId);

    List<CategoryCostDto> getTransaction(Integer type, String category, String startDate, String endDate);

    UpdateTransactionDto updateTransaction(UpdateTransactionDto updatetransactionDto);
}
