package com.example.accountbot;

import com.example.accountbot.dto.ai.AIFeedbackDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.dto.transaction.BalanceDto;
import com.example.accountbot.service.*;
import com.example.accountbot.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import java.io.IOException;

@LineMessageHandler
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final TransactionService transactionService;

    private final ChartGenerateService chartGenerateService;

    private final S3Service s3Service;

    private final BudgetService budgetService;

    private final CategoryService categoryService;

    private final AIService aiService;

    @Autowired
    private RedisUtil redisUtil;

    private final String CACHE_KEY = "transaction:";

    public static final int EXPENSE_CATEGORY = 1;
    public static final int INCOME_CATEGORY = 0;

    public static final Integer FULL_BUDGET = 1;
    public static final double HALF_BUDGET = 0.5;

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException, IOException {
        String userId = event.getSource().getUserId();
        String receivedText = event.getMessage().getText();

        try {

            if (receivedText == null || receivedText.isEmpty()) {
                log.info("空的訊息內容");
                return;
            }

            if (receivedText.startsWith("支出報表") || receivedText.startsWith("收入報表")) {

                int type = receivedText.startsWith("收入報表") ? INCOME_CATEGORY : EXPENSE_CATEGORY;

                String timePeriod;
                if (receivedText.contains("昨日")) {
                    timePeriod = "yesterday";
                } else if (receivedText.contains("本週")) {
                    timePeriod = "week";
                } else if (receivedText.contains("本月")) {
                    timePeriod = "month";
                } else if (receivedText.contains("半年")) {
                    timePeriod = "halfYear";
                } else {
                    timePeriod = "month";
                }

                String outputFilePath = "src/main/resources/static/images/transactionPieChart.png";
                String imagePath = chartGenerateService.generatePieChart(type, timePeriod, outputFilePath, userId);

                if (imagePath != null) {
                    log.info("圖片生成並儲存成功，路徑為: " + imagePath);
                } else {
                    log.info("圖片生成失敗");
                }

                String displayTextPrefix = receivedText.startsWith("收入報表") ? "收入報表" : "支出報表";

                String flexMessageJson = String.format("""
        {
            "type": "bubble",
            "size": "giga",
            "hero": {
                "type": "image",
                "url": "%s",
                "size": "full",
                "aspectRatio": "20:15",
                "aspectMode": "cover",
                "action": {
                    "type": "uri",
                    "uri": "https://jacktest.site/index.html"
                }
            },
            "footer": {
                "type": "box",
                "layout": "horizontal",
                "contents": [
                    {
                        "type": "button",
                        "action": {
                            "type": "message",
                            "label": "昨日",
                            "text": "%s:昨日"
                        },
                        "color": "#F7D486",
                        "style": "secondary",
                        "height": "md",
                        "margin": "md"
                    },
                    {
                        "type": "button",
                        "action": {
                            "type": "message",
                            "label": "本週",
                            "text": "%s:本週"
                        },
                        "color": "#F7D486",
                        "style": "secondary",
                        "height": "md",
                        "margin": "md"
                    },
                    {
                        "type": "button",
                        "action": {
                            "type": "message",
                            "label": "本月",
                            "text": "%s:本月"
                        },
                        "color": "#F7D486",
                        "style": "secondary",
                        "height": "md",
                        "margin": "md"
                    },
                    {
                        "type": "button",
                        "action": {
                            "type": "message",
                            "label": "半年",
                            "text": "%s:半年"
                        },
                        "color": "#F7D486",
                        "style": "secondary",
                        "height": "md",
                        "margin": "md"
                    }
                ]
            }
        }
    """, "%s", displayTextPrefix, displayTextPrefix, displayTextPrefix, displayTextPrefix);

                JSONObject flexMessageJsonObject = new JSONObject(flexMessageJson);

                String s3Key = "images/transaction/" + UUID.randomUUID() + ".png";
                String imageUrl = uploadImageToS3(imagePath, s3Key);

                flexMessageJsonObject.getJSONObject("hero")
                        .put("url", imageUrl);

                String updatedFlexMessageJson = flexMessageJsonObject.toString();

                try {
                    FlexContainer flexContainer = objectMapper.readValue(updatedFlexMessageJson, FlexContainer.class);
                    FlexMessage flexMessage = new FlexMessage("圖表分析", flexContainer);
                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();
                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            else if (receivedText.matches("\\$.*")) {
                try {

                    if (!validateCost(receivedText, userId)) {
                        return;
                    }

                    redisUtil.setDataToCache(CACHE_KEY + userId, receivedText);

                    Map<String, Object> expenseCategoryMap = categoryService.get(EXPENSE_CATEGORY, "all", userId);
                    List<UpdateCategoryDto> expenseCategories = (List<UpdateCategoryDto>) expenseCategoryMap.get("data");

                    StringBuilder buttonContents = new StringBuilder();
                    int buttonCount = 0;

                    for (int i = 0; i < expenseCategories.size(); i++) {
                        UpdateCategoryDto category = expenseCategories.get(i);
                        String categoryName = category.getName();

                        if (buttonCount % 3 == 0) {
                            if (buttonCount != 0) {
                                buttonContents.append("]},");
                            }
                            buttonContents.append("""
                {
                    "type": "box",
                    "layout": "horizontal",
                    "contents": [
                """);
                        }

                        buttonContents.append("""
            {
                "type": "button",
                "action": {
                    "type": "postback",
                    "label": "%s",
                    "data": "%s",
                    "displayText": "支出分類選擇:%s類別"
                },
                "color": "#F7D486",
                "style": "secondary",
                "height": "md",
                "margin": "md",
                "height": "sm",
                "flex":0,
                "margin": "md"
            }
            """.formatted(categoryName, categoryName, categoryName));

                        buttonCount++;

                        if (buttonCount % 3 != 0 && i < expenseCategories.size() - 1) {
                            buttonContents.append(",");
                        }
                    }

                    if (buttonCount > 0) {
                        buttonContents.append("]}");
                    }

                    String flexMessageJson = """
        {
            "type": "bubble",
            "size": "mega",
            "body": {
                "type": "box",
                "layout": "vertical",
                "contents": [
                    %s
                ],
                "spacing": "sm"
            }
        }
        """.formatted(buttonContents.toString());

                    FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);
                    FlexMessage flexMessage = new FlexMessage("記支出", flexContainer);

                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    sendLineMessage(userId, "記帳失敗");
                }
            }

            else if (receivedText.matches("\\+.*")) {
                try {

                    if (!validateCost(receivedText, userId)) {
                        return;
                    }

                    redisUtil.setDataToCache(CACHE_KEY + userId, receivedText);

                    Map<String, Object> incomeCategoryMap = categoryService.get(INCOME_CATEGORY, "all", userId);
                    List<UpdateCategoryDto> incomeCategories = (List<UpdateCategoryDto>) incomeCategoryMap.get("data");

                    StringBuilder buttonContents = new StringBuilder();
                    int buttonCount = 0;

                    for (int i = 0; i < incomeCategories.size(); i++) {
                        UpdateCategoryDto category = incomeCategories.get(i);
                        String categoryName = category.getName();

                        if (buttonCount % 3 == 0) {
                            if (buttonCount != 0) {
                                buttonContents.append("]},");
                            }
                            buttonContents.append("""
                {
                    "type": "box",
                    "layout": "horizontal",
                    "contents": [
                """);
                        }

                        buttonContents.append("""
            {
                "type": "button",
                "action": {
                    "type": "postback",
                    "label": "%s",
                    "data": "%s",
                    "displayText": "收入分類選擇:%s類別"
                },
                "color": "#F7D486",
                "style": "secondary",
                "height": "md",
                "margin": "md",
                "height": "sm",
                "flex": 0,
                "margin": "md"
            }
            """.formatted(categoryName, categoryName, categoryName));

                        buttonCount++;

                        if (buttonCount % 3 != 0 && i < incomeCategories.size() - 1) {
                            buttonContents.append(",");
                        }
                    }

                    if (buttonCount > 0) {
                        buttonContents.append("]}");
                    }

                    String flexMessageJson = """
        {
            "type": "bubble",
            "size": "mega",
            "body": {
                "type": "box",
                "layout": "vertical",
                "contents": [
                    %s
                ],
                "spacing": "sm"
            }
        }
        """.formatted(buttonContents.toString());

                    FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);
                    FlexMessage flexMessage = new FlexMessage("記收入", flexContainer);

                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    sendLineMessage(userId, "記帳失敗");
                }
            }

            else if (receivedText.matches("^刪除.*")){
                String[] parts = receivedText.split(":");
                if (parts.length == 2) {
                    String type = receivedText.substring(2);
                    int transactionId = Integer.parseInt(parts[1].trim());

                    transactionService.delete(transactionId);

                    sendLineMessage(userId, type + " 已成功刪除");
                }
            }

            else if (receivedText.matches("^本月結餘.*")) {

                BalanceDto balanceDto = transactionService.balance(userId);
                log.info("balanceDto : " + balanceDto);

                int totalIncome = balanceDto.getTotalIncome() != null ? balanceDto.getTotalIncome() : 0;
                int totalExpenses = balanceDto.getTotalExpenses() != null ? balanceDto.getTotalExpenses() : 0;
                int netBalance = totalIncome - totalExpenses;

                String message = String.format(
                        "本月結餘：收入(%d) - 支出(%d) = %d 元\n\n累積資產：%d 元",
                        totalIncome, totalExpenses, netBalance, netBalance
                );

                TextMessage textMessage = new TextMessage(message);

                String outputFilePath = "src/main/resources/static/images/balanceBarChart.png";
                String imagePath = chartGenerateService.generateBarChart(totalIncome, totalExpenses, netBalance, outputFilePath);

                if (imagePath != null) {
                    log.info("圖片生成並儲存成功，路徑為: " + imagePath);
                } else {
                    log.info("圖片生成失敗");
                }

                lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

                String flexMessageJson = """
            {
                "type": "bubble",
                "size": "giga",
                "hero": {
                    "type": "image",
                    "size": "full",
                    "aspectMode": "cover",
                    "action": {
                        "type": "uri",
                        "uri": "https://jacktest.site/index.html"
                    },
                    "url": "%s.png",
                    "aspectRatio": "20:13"
                }
            }
        """;

                JSONObject flexMessageJsonObject = new JSONObject(flexMessageJson);

                String s3Key = "images/balance/" + UUID.randomUUID() + ".png";
                String imageUrl = uploadImageToS3(imagePath, s3Key);

                flexMessageJsonObject.getJSONObject("hero")
                        .put("url", imageUrl);

                String updatedFlexMessageJson = flexMessageJsonObject.toString();

                try {

                    FlexContainer flexContainer = objectMapper.readValue(updatedFlexMessageJson, FlexContainer.class);

                    FlexMessage flexMessage = new FlexMessage("本月結餘", flexContainer);

                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    sendLineMessage(userId, "圖表生成失敗");
                }

            }

            else if (receivedText.matches("^匯出上月報表.*")) {

                ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
                LocalDate today = LocalDate.now(taipeiZone);
                LocalDate startDate = today.minusMonths(1).withDayOfMonth(1);
                LocalDate endDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

                String startDateStr = dateToString(startDate);
                String endDateStr = dateToString(endDate);

                String message = s3Service.exportCsvFile(userId, transactionService.getAllTransaction(startDateStr, endDateStr, userId));

                TextMessage textMessage = new TextMessage("請點擊網址下載上個月報表的 CSV 檔"+ "\n" + message);

                lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

            }

            else if (receivedText.matches("^分析上月報表.*")) {

                Map<String, Object> expense = transactionService.getTransaction(EXPENSE_CATEGORY, "all", "lastMonth", userId);
                Map<String, Object> income = transactionService.getTransaction(INCOME_CATEGORY, "all", "lastMonth", userId);

                Map<String, Object> response = aiService.getFeedback(expense, income);

                AIFeedbackDto data = (AIFeedbackDto) response.get("data");

                String analysisMessage = data.getAnalysis();
                String adviceMessage = data.getAdvice();

                TextMessage textMessage = new TextMessage("理財分析:"+ "\n" + analysisMessage + "\n\n" + "理財建議:"+ "\n" + adviceMessage);
                lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

            }

            else if ((receivedText.matches("^指令.*") || receivedText.matches("^教學.*"))) {
                String flexMessageJson = String.format("""
        {
            "type": "bubble",
            "body": {
                "type": "box",
                "layout": "vertical",
                "contents": [
                    {
                        "type": "text",
                        "text": "使用教學如下 :",
                        "weight": "bold",
                        "size": "xl"
                    },
                    {
                        "type": "text",
                        "text": "紀錄支出",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: $(金額)(空格)(備註)\\nex. $100 午餐",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "紀錄收入",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: +(金額)(空格)(備註)\\nex. +100 零用錢",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "(目前收入、支出分類皆為預設，若想調整請至官網>立即記帳>分類管理)",
                        "wrap": true,
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "查看支出狀況",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: 支出報表",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "查看收入狀況",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: 收入報表",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "查看結餘",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: 本月結餘",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "匯出成 CSV 檔",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: 匯出上月報表",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "理財分析與建議",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: 分析上月報表",
                        "wrap": true
                    },
                    {
                        "type": "text",
                        "text": "查詢指令",
                        "weight": "bold",
                        "size": "md",
                        "margin": "md"
                    },
                    {
                        "type": "text",
                        "text": "輸入: 指令\\n輸入: 教學",
                        "wrap": true
                    }
                ]
            },
            "footer": {
                "type": "box",
                "layout": "horizontal",
                "contents": [
                    {
                        "type": "button",
                        "action": {
                            "type": "uri",
                            "label": "官網",
                            "uri": "https://jacktest.site"
                        },
                        "color": "#F7D486",
                        "style": "primary"
                    }
                ]
            }
        }
        """);

                try {
                    JSONObject flexMessageJsonObject = new JSONObject(flexMessageJson);
                    FlexContainer flexContainer = objectMapper.readValue(flexMessageJsonObject.toString(), FlexContainer.class);
                    FlexMessage flexMessage = new FlexMessage("教學內容", flexContainer);
                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else {
                log.info("error: 指令有誤");
                sendLineMessage(userId, "請輸入正確指令");
            }

        }catch (Exception e){
            e.printStackTrace();
            log.info("error: " + e.getMessage());
            sendLineMessage(userId, "發生錯誤，請稍後再試");
        }

    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String userId = event.getSource().getUserId();
        String postbackData = event.getPostbackContent().getData();

        Integer type = -1;

        try {
            Map<String, Object> expenseCategoryMap = categoryService.get(EXPENSE_CATEGORY, "all", userId);
            Map<String, Object> incomeCategoryMap = categoryService.get(INCOME_CATEGORY, "all", userId);

            List<UpdateCategoryDto> expenseCategories = (List<UpdateCategoryDto>) expenseCategoryMap.get("data");
            List<UpdateCategoryDto> incomeCategories = (List<UpdateCategoryDto>) incomeCategoryMap.get("data");

            Map<String, String> categoryMessages = new HashMap<>();
            Map<String, Integer> categoryTypes = new HashMap<>();

            for (UpdateCategoryDto category : expenseCategories) {
                String categoryName = category.getName();
                categoryMessages.put(categoryName, "支出分類選擇:" + categoryName + "類別");
                categoryTypes.put(categoryName, 1);
            }

            for (UpdateCategoryDto category : incomeCategories) {
                String categoryName = category.getName();
                categoryMessages.put(categoryName, "收入分類選擇:" + categoryName + "類別");
                categoryTypes.put(categoryName, 0);
            }

            if (categoryMessages.containsKey(postbackData)) {
                type = categoryTypes.get(postbackData);
                String typeStr = "";
                String lastUserMessage = redisUtil.getDataFromCache(CACHE_KEY + userId);

                String cleanedMessage = lastUserMessage.trim().replaceAll("\\s+", " ");

                String[] parts = cleanedMessage.substring(1).trim().split("\\s+", 2);

                String amountStr = parts[0].trim();
                int amount = Integer.parseInt(amountStr);

                String description = parts.length > 1 ? parts[1].trim() : "";

                ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
                LocalDate today = LocalDate.now(taipeiZone);
                String todayStr = transactionService.dateToString(today);

                Integer transactionId = transactionService.createTransaction(type, postbackData, amount, description, todayStr, userId);

                if (type == EXPENSE_CATEGORY) {
                    budgetAlert(postbackData, userId, type);
                }

                String imagePath = chartGenerateService.generateRecordImage(type, amount, postbackData, userId);

                if (imagePath == null) {
                    sendLineMessage(userId, "記帳失敗，無法生成圖像");
                    return;
                }

                String s3Key = "images/record/" + UUID.randomUUID() + ".png";
                String imageUrl = uploadImageToS3(imagePath, s3Key);

                if (imageUrl == null) {
                    sendLineMessage(userId, "記帳失敗，無法上傳圖片至 S3");
                    return;
                }

                if(type.equals(INCOME_CATEGORY)){
                    typeStr = "收入";
                }else if(type.equals(EXPENSE_CATEGORY)){
                    typeStr = "支出";
                }

                String flexMessageJson = """
    {
        "type": "bubble",
        "size": "giga",
        "hero": {
            "type": "image",
            "url": "%s",
            "size": "full",
            "aspectRatio": "2:1",
            "aspectMode": "cover",
            "action": {
            "type": "uri",
            "uri": "https://jacktest.site/index.html"
            }
        },
        "body": {
            "type": "box",
            "layout": "horizontal",
            "contents": [
            {
                "type": "button",
                "action": {
                "type": "uri",
                "label": "編輯",
                "uri": "https://jacktest.site/transactionDetail.html?lineUserId=%s&transactionId=%s"
                },
                "color": "#F7D486",
                "style": "secondary",
                "height": "md",
                "margin": "md"
            },
            {
                "type": "button",
                "action": {
                "type": "message",
                "label": "刪除",
                "text": "刪除%s ID:%s"
                },
                "color": "#F7D486",
                "style": "secondary",
                "height": "md",
                "margin": "md"
            },
            {
                "type": "button",
                "action": {
                "type": "message",
                "label": "報表",
                "text": "%s報表"
                },
                "color": "#F7D486",
                "style": "secondary",
                "height": "md",
                "margin": "md"
            },
            {
                "type": "button",
                "action": {
                "type": "message",
                "label": "結餘",
                "text": "本月結餘"
                },
                "color": "#F7D486",
                "style": "secondary",
                "height": "md",
                "margin": "md"
            }
            ]
        }
        }
""".formatted(imageUrl, userId, transactionId, typeStr, transactionId, typeStr);

                FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);
                FlexMessage flexMessage = new FlexMessage("記帳資訊", flexContainer);

                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public String uploadImageToS3(String filePath, String s3Key) {

        String imageUrl = s3Service.uploadFile(filePath, s3Key);
        log.info("Image uploaded to S3, URL: " + imageUrl);

        return imageUrl;
    }

    public void budgetAlert(String category, String lineUserId, Integer type) {

        try {
            Map<String, Object> budgetData = budgetService.get(category, lineUserId);

            Map<String, Object> transactionData = transactionService.getTransaction(type , category, "month", lineUserId);

            JSONObject budgetObject = new JSONObject(budgetData);
            JSONArray budgetArray = budgetObject.getJSONArray("data");
            JSONObject budgetItem = budgetArray.getJSONObject(0);
            Integer budgetPrice = budgetItem.getInt("price");

            JSONObject transactionObject = new JSONObject(transactionData);
            JSONArray transactionArray = transactionObject.getJSONArray("data");
            JSONObject transactionItem = transactionArray.getJSONObject(0);
            Integer totalCost = transactionItem.getInt("totalCost");

            if(totalCost / budgetPrice >= FULL_BUDGET){
                sendLineMessage(lineUserId, "已超出 " + category + " 預算");
            } else if ((double)totalCost / budgetPrice >= HALF_BUDGET) {
                sendLineMessage(lineUserId, "已超出 " + category + " 一半預算");
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info("error: " + e.getMessage());
        }


    }

    private void sendLineMessage(String lineUserId, String description) {
        TextMessage textMessage = new TextMessage(description);
        PushMessage pushMessage = new PushMessage(lineUserId, textMessage);
        lineMessagingClient.pushMessage(pushMessage);
    }

    public String dateToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return date.format(formatter);
    }

    public boolean validateCost(String receivedText, String userId) {
        try {
            String costString = receivedText.split(" ")[0];
            int cost = Integer.parseInt(costString.substring(1));

            if (cost > 999999) {
                sendLineMessage(userId, "金額不得超過 6 位數！");
                return false;
            }

            if (cost <= 0) {
                sendLineMessage(userId, "金額必須大於 0！");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            sendLineMessage(userId, "金額格式不正確！");
            return false;
        }
    }

}