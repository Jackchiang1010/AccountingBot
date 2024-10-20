package com.example.accountbot;

import com.example.accountbot.controller.TransactionController;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void record_transaction() throws Exception {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setType(1);
        transactionDto.setCategory("飲食");
        transactionDto.setCost(100);
        transactionDto.setDescription("Lunch");
        transactionDto.setDate("2024-10-10");
        transactionDto.setLineUserId("U6f5de7e36ccc512ef1b72bf881e87e54");

        String recordData = objectMapper.writeValueAsString(transactionDto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        when(transactionService.record(any(TransactionDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/1.0/transaction/record")
                        .contentType("application/json")
                        .content(recordData))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    @Test
    public void record_income_transaction() throws Exception {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setType(0);
        transactionDto.setCategory("薪水");
        transactionDto.setCost(100);
        transactionDto.setDescription("Lunch");
        transactionDto.setDate("2024-10-10");
        transactionDto.setLineUserId("U6f5de7e36ccc512ef1b72bf881e87e54");

        String recordData = objectMapper.writeValueAsString(transactionDto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        when(transactionService.record(any(TransactionDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/1.0/transaction/record")
                        .contentType("application/json")
                        .content(recordData))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    @Test
    public void record_transaction_with_invalid_data() throws Exception {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setType(1);
        transactionDto.setCategory(null);
        transactionDto.setCost(100);
        transactionDto.setDescription("Lunch");
        transactionDto.setDate("2024-10-10");
        transactionDto.setLineUserId("U6f5de7e36ccc512ef1b72bf881e87e54");

        when(transactionService.record(any(TransactionDto.class)))
                .thenThrow(new IllegalArgumentException("Category cannot be null"));

        String recordData = objectMapper.writeValueAsString(transactionDto);

        mockMvc.perform(post("/api/1.0/transaction/record")
                        .contentType("application/json")
                        .content(recordData))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void get_transaction() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", Collections.emptyList());
        when(transactionService.getTransaction(anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/1.0/transaction/get")
                        .queryParam("type", "1")
                        .queryParam("category", "1")
                        .queryParam("time", "2024-10-10")
                        .queryParam("lineUserId", "U6f5de7e36ccc512ef1b72bf881e87e54"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"transactions\":[]}"));
    }
}
