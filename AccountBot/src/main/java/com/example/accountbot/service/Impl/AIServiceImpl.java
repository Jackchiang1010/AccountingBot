package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.ai.AIFeedbackDto;
import com.example.accountbot.service.AIService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Override
    public Map<String, Object> getFeedback(Map<String, Object> expense, Map<String, Object> income) {

        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage("請用最簡單的純文字格式回覆，不能使用任何 Markdown 語法，直接列出分析內容。"));
        messages.add(new SystemMessage("你現在是理財達人"));
        messages.add(new SystemMessage("這是使用者的支出狀況" + expense));
        messages.add(new SystemMessage("這是使用者的收入狀況" + income));
        messages.add(new SystemMessage("請提供最簡短的回覆，避免重述收入和支出的數字。"));

        messages.add(new SystemMessage("只需總結3個最關鍵的觀察點。"));

        ChatResponse responseAnalysis = openAiChatModel
                .call(new Prompt(
                        messages,
                        OpenAiChatOptions.builder()
                                .withTemperature(0f)
                                .build()
                ));

        String analysis = responseAnalysis.getResult().getOutput().getContent();

        messages.add(new SystemMessage("只需總結3個最具行動性的建議即可。"));

        ChatResponse responseAdvice = openAiChatModel
                .call(new Prompt(
                        messages,
                        OpenAiChatOptions.builder()
                                .withTemperature(0f)
                                .build()
                ));

        String advice = responseAdvice.getResult().getOutput().getContent();

        AIFeedbackDto aiFeedbackDto = new AIFeedbackDto();
        aiFeedbackDto.setAnalysis(analysis);
        aiFeedbackDto.setAdvice(advice);

        Map<String, Object> result = new HashMap<>();
        result.put("data", aiFeedbackDto);

        return result;
    }
}
