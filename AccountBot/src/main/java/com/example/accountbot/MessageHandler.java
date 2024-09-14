package com.example.accountbot;

import com.example.accountbot.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@LineMessageHandler
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final TransactionService transactionService;

    private String lastUserMessage = "";

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.info("event: " + event);
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String userId = event.getSource().getUserId();
        String receivedText = event.getMessage().getText();

        lastUserMessage = receivedText;

        // 如果用戶輸入以 "+" 開頭，則回應 Flex Message
        if (receivedText != null && receivedText.matches("\\$.*")) {

            String flexMessageJson = """
                            {
                              "type": "bubble",
                              "body": {
                                "type": "box",
                                "layout": "horizontal",
                                "contents": [
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "飲食",
                                      "data": "飲食",
                                      "displayText": "test1"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "娛樂",
                                      "data": "娛樂",
                                      "displayText": "test2"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "交通",
                                      "data": "交通",
                                      "displayText": "test3"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "藥妝",
                                      "data": "藥妝",
                                      "displayText": "test4"
                                    },
                                    "position": "relative"
                                  }
                                ]
                              }
                            }
                    """;

            try {
                // 將 JSON 字串轉換為 FlexContainer
                FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);

                // 建立 FlexMessage
                FlexMessage flexMessage = new FlexMessage("Flex Message 標題", flexContainer);

                // 發送 Flex Message
                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String userId = event.getSource().getUserId();
        String postbackData = event.getPostbackContent().getData();

        try {
            if ("飲食".equals(postbackData)) {
                // 處理“飲食”類別選擇
                TextMessage replyMessage = new TextMessage("支出分類選擇:飲食類別");
                lineMessagingClient.pushMessage(new PushMessage(userId, replyMessage)).get();
            } else if ("娛樂".equals(postbackData)) {
                // 處理“娛樂”類別選擇
                TextMessage replyMessage = new TextMessage("支出分類選擇:娛樂類別");
                lineMessagingClient.pushMessage(new PushMessage(userId, replyMessage)).get();
            } else if ("交通".equals(postbackData)) {
                // 處理“交通”類別選擇
                TextMessage replyMessage = new TextMessage("支出分類選擇:交通類別");
                lineMessagingClient.pushMessage(new PushMessage(userId, replyMessage)).get();
            } else if ("藥妝".equals(postbackData)) {
                // 處理“藥妝”類別選擇
                TextMessage replyMessage = new TextMessage("支出分類選擇:藥妝類別");
                lineMessagingClient.pushMessage(new PushMessage(userId, replyMessage)).get();
            }

            // 預處理輸入字串：去除首尾空白並將多個空白字符替換為單個空格
            String cleanedMessage = lastUserMessage.trim().replaceAll("\\s+", " ");

            // 以空格分割字串，限制最多分成兩部分：金額和描述
            String[] parts = cleanedMessage.substring(1).trim().split("\\s+", 2);

            // 解析金額部分
            String amountStr = parts[0].trim();
            log.info("amountStr: " + amountStr);
            int amount = Integer.parseInt(amountStr);

            // 解析描述部分（如果有）
            String description = parts.length > 1 ? parts[1].trim() : "";
            log.info("description: " + description);

            ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
            LocalDate today = LocalDate.now(taipeiZone);
            String todayStr = transactionService.dateToString(today);

            log.info("postbackData : " + postbackData);
            log.info("todayStr : " +  todayStr);
            log.info("userId : " +  userId);

            transactionService.createTransaction(1,postbackData, amount, description, todayStr, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢復中斷狀態
            e.printStackTrace();
            // 可以考慮記錄錯誤或通知管理員
        } catch (ExecutionException e) {
            e.printStackTrace();
            // 可以考慮記錄錯誤或通知管理員
        }
    }

}