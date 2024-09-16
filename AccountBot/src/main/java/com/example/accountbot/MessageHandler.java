package com.example.accountbot;

import com.example.accountbot.dto.transaction.BalanceDto;
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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException {
        String userId = event.getSource().getUserId();
        String receivedText = event.getMessage().getText();

        lastUserMessage = receivedText;

        // 如果用戶輸入以 "$" 開頭
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
                                      "displayText": "支出分類選擇:飲食類別"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "娛樂",
                                      "data": "娛樂",
                                      "displayText": "支出分類選擇:娛樂類別"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "交通",
                                      "data": "交通",
                                      "displayText": "支出分類選擇:交通類別"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "藥妝",
                                      "data": "藥妝",
                                      "displayText": "支出分類選擇:藥妝類別"
                                    },
                                    "position": "relative"
                                  }
                                ]
                              }
                            }
                    """;

            try {

                FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);

                FlexMessage flexMessage = new FlexMessage("Flex Message 標題", flexContainer);

                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 如果用戶輸入以 "+" 開頭
        if (receivedText != null && receivedText.matches("\\+.*")) {

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
                                      "label": "薪水",
                                      "data": "薪水",
                                      "displayText": "收入分類選擇:薪水類別"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "獎金",
                                      "data": "獎金",
                                      "displayText": "收入分類選擇:獎金類別"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "兼職",
                                      "data": "兼職",
                                      "displayText": "收入分類選擇:兼職類別"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "postback",
                                      "label": "投資",
                                      "data": "投資",
                                      "displayText": "收入分類選擇:投資類別"
                                    },
                                    "position": "relative"
                                  }
                                ]
                              }
                            }
                    """;

            try {

                FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);

                FlexMessage flexMessage = new FlexMessage("Flex Message 標題", flexContainer);

                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 如果用戶輸入以 "結餘" 開頭
        if (receivedText != null && receivedText.matches("^結餘.*")) {

            BalanceDto balanceDto = transactionService.balance();
            int totalIncome = balanceDto.getTotalIncome();
            int totalExpenses = balanceDto.getTotalExpenses();
            int netBalance = totalIncome - totalExpenses;

            String message = String.format(
                    "本月結餘：收入(%d) - 支出(%d) = %d 元\n\n累積資產：%d 元",
                    totalIncome, totalExpenses, netBalance, netBalance
            );

            TextMessage textMessage = new TextMessage(message);

            lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

            String flexMessageJson = """
            {
                "type": "bubble",
                "hero": {
                  "type": "image",
                  "url": "https://developers-resource.landpress.line.me/fx/img/01_1_cafe.png",
                  "size": "full",
                  "aspectRatio": "20:13",
                  "aspectMode": "cover",
                  "action": {
                    "type": "uri",
                    "uri": "https://line.me/"
                  }
                },
                "body": {
                  "type": "box",
                  "layout": "vertical",
                  "contents": [
                    {
                      "type": "box",
                      "layout": "horizontal",
                      "contents": [
                        {
                          "type": "text",
                          "text": "收入",
                          "align": "start"
                        },
                        {
                          "type": "text",
                          "text": "結餘",
                          "align": "center"
                        },
                        {
                          "type": "text",
                          "text": "支出",
                          "align": "end"
                        }
                      ]
                    },
                    {
                      "type": "box",
                      "layout": "horizontal",
                      "contents": [
                        {
                          "type": "text",
                          "text": "$",
                          "align": "start"
                        },
                        {
                          "type": "text",
                          "text": "$",
                          "align": "center"
                        },
                        {
                          "type": "text",
                          "text": "$",
                          "align": "end"
                        }
                      ]
                    }
                  ]
                }
              }
        """;

            JSONObject flexMessageJsonObject = new JSONObject(flexMessageJson);

            flexMessageJsonObject.getJSONObject("body")
                    .getJSONArray("contents")
                    .getJSONObject(1)
                    .getJSONArray("contents")
                    .getJSONObject(0)
                    .put("text", totalIncome);
            flexMessageJsonObject.getJSONObject("body")
                    .getJSONArray("contents")
                    .getJSONObject(1)
                    .getJSONArray("contents")
                    .getJSONObject(1)
                    .put("text", netBalance);
            flexMessageJsonObject.getJSONObject("body")
                    .getJSONArray("contents")
                    .getJSONObject(1)
                    .getJSONArray("contents")
                    .getJSONObject(2)
                    .put("text", totalExpenses);

            String updatedFlexMessageJson = flexMessageJsonObject.toString();

            try {

                FlexContainer flexContainer = objectMapper.readValue(updatedFlexMessageJson, FlexContainer.class);

                FlexMessage flexMessage = new FlexMessage("Flex Message 標題", flexContainer);

                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }

    }

    //記帳，flexMessage 內放 postback
    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String userId = event.getSource().getUserId();
        String postbackData = event.getPostbackContent().getData();

        Integer type = -1;

        try {
            // Map for storing category messages and types
            Map<String, String> categoryMessages = new HashMap<>();
            categoryMessages.put("飲食", "支出分類選擇:飲食類別");
            categoryMessages.put("娛樂", "支出分類選擇:娛樂類別");
            categoryMessages.put("交通", "支出分類選擇:交通類別");
            categoryMessages.put("藥妝", "支出分類選擇:藥妝類別");
            categoryMessages.put("薪水", "收入分類選擇:薪水類別");
            categoryMessages.put("獎金", "收入分類選擇:獎金類別");
            categoryMessages.put("兼職", "收入分類選擇:兼職類別");
            categoryMessages.put("投資", "收入分類選擇:投資類別");

            // Determine the type based on the category
            Map<String, Integer> categoryTypes = new HashMap<>();
            categoryTypes.put("飲食", 1);
            categoryTypes.put("娛樂", 1);
            categoryTypes.put("交通", 1);
            categoryTypes.put("藥妝", 1);
            categoryTypes.put("薪水", 0);
            categoryTypes.put("獎金", 0);
            categoryTypes.put("兼職", 0);
            categoryTypes.put("投資", 0);

            // Check if the postbackData is in the map
            if (categoryMessages.containsKey(postbackData)) {
                String replyText = categoryMessages.get(postbackData);
                TextMessage replyMessage = new TextMessage(replyText);
                lineMessagingClient.pushMessage(new PushMessage(userId, replyMessage)).get();
                type = categoryTypes.get(postbackData);
            }

            // 預處理輸入字串：去除首尾空白並將多個空白字符替換為單個空格
            String cleanedMessage = lastUserMessage.trim().replaceAll("\\s+", " ");

            // 以空格分割字串，限制最多分成兩部分：金額和描述
            String[] parts = cleanedMessage.substring(1).trim().split("\\s+", 2);

            String amountStr = parts[0].trim();
            log.info("amountStr: " + amountStr);
            int amount = Integer.parseInt(amountStr);

            String description = parts.length > 1 ? parts[1].trim() : "";

            ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
            LocalDate today = LocalDate.now(taipeiZone);
            String todayStr = transactionService.dateToString(today);

            transactionService.createTransaction(type,postbackData, amount, description, todayStr, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢復中斷狀態
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}