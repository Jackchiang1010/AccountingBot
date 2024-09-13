package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.TransactionDto;
import com.example.accountbot.repository.TransactionRepository;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
}
