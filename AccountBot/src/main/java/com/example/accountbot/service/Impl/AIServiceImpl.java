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

        messages.add(new SystemMessage("禁止使用 Markdown 語法回覆"));
        messages.add(new SystemMessage("你現在是理財達人"));
        messages.add(new SystemMessage("這是使用者的支出狀況" + expense));
        messages.add(new SystemMessage("這是使用者的收入狀況" + income));

        messages.add(new SystemMessage("請給予精簡的理財分析，不用給建議"));

        ChatResponse responseAnalysis = openAiChatModel
                .call(new Prompt(
                        messages,
                        OpenAiChatOptions.builder()
                                .withTemperature(0f)
                                .build()
                ));

        String analysis = responseAnalysis.getResult().getOutput().getContent();

        messages.add(new SystemMessage("請給予精簡的理財建議，不用分析"));

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
