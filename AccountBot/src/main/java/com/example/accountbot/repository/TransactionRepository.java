package com.example.accountbot.repository;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.dto.transaction.BalanceDto;
import com.example.accountbot.dto.transaction.GetAllTransactionDto;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;

import java.util.List;

public interface TransactionRepository {

    Integer recordTransaction(TransactionDto transactionDto);
    Integer getCategoryId(String categoryName, String lineUserId);

    List<CategoryCostDto> getTransaction(Integer type, String category, String startDate, String endDate ,String lineUserId);

    UpdateTransactionDto updateTransaction(UpdateTransactionDto updatetransactionDto);

    boolean delete(Integer id);

    BalanceDto balance(String startDate, String endDate);

    List<GetAllTransactionDto> getAllTransaction(String startDate, String endDate, String lineUserId);
}
