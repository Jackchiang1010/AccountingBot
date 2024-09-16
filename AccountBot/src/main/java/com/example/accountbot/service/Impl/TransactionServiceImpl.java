package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.dto.transaction.BalanceDto;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;
import com.example.accountbot.repository.TransactionRepository;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Map<String, Object> record(TransactionDto transactionDto) {

        Integer transactionId = transactionRepository.recordTransaction(transactionDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", transactionId);

        return result;
    }

    @Override
    public Map<String, Object> getTransaction(Integer type, String category, String time, String lineUserId) {

        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        LocalDate today = LocalDate.now(taipeiZone); // 取得台灣時間的今天日期
        LocalDate startDate = null;
        LocalDate endDate = today; // 預設結束時間為今天

        switch (time) {
            case "today":
                startDate = today;
                break;
            case "yesterday":
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;
            case "week":
                startDate = today.minusWeeks(1);
                break;
            case "month":
                startDate = today.minusMonths(1);
                break;
            case "lastMonth":
                startDate = today.minusMonths(1).withDayOfMonth(1);
                endDate = startDate.plusMonths(1).minusDays(1);
                break;
            case "halfYear":
                startDate = today.minusMonths(6);
                break;
            default:
                throw new IllegalArgumentException("Invalid time parameter");
        }

        String startDateStr = dateToString(startDate);
        String endDateStr = dateToString(endDate);

        List<CategoryCostDto> getTransactionDto = transactionRepository.getTransaction(type, category, startDateStr, endDateStr, lineUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", getTransactionDto);

        return result;
    }

    @Override
    public void createTransaction(Integer type, String category, Integer cost, String description, String date, String lineUserId) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setType(type);
        transactionDto.setCategory(category);
        transactionDto.setCost(cost);
        transactionDto.setDescription(description);
        transactionDto.setDate(date);
        transactionDto.setLineUserId(lineUserId);

        record(transactionDto);
    }

    @Override
    public String dateToString(LocalDate date) {
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        LocalDate today = LocalDate.now(taipeiZone); // 取得台灣時間的今天日期
        // 定義日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 將 LocalDate 轉換為 String
        String dateStr = date.format(formatter);
        return dateStr;
    }

    @Override
    public Map<String, Object> update(UpdateTransactionDto updatetransactionDto) {

        UpdateTransactionDto updatedTransaction = transactionRepository.updateTransaction(updatetransactionDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", updatedTransaction);

        return result;
    }

    @Override
    public boolean delete(Integer id) {
        return transactionRepository.delete(id);
    }

    @Override
    public BalanceDto balance() {

        BalanceDto balanceDto = transactionRepository.balance();

        return balanceDto;
    }
}
