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

    private String lastUserMessage = "";

    private final ChartGenerateService chartGenerateService;

    private final S3Service s3Service;

    private final BudgetService budgetService;

    private final CategoryService categoryService;

    private final AIService aiService;

    @Autowired
    private RedisUtil redisUtil;

    private String CACHE_KEY = "transaction:";

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException, IOException {
        String userId = event.getSource().getUserId();
        String receivedText = event.getMessage().getText();

        redisUtil.setDataToCache(CACHE_KEY + userId, receivedText);

//        lastUserMessage = receivedText;

        try {

            if (receivedText != null && (receivedText.startsWith("支出報表") || receivedText.startsWith("收入報表"))) {

                // 根據開頭決定第一個參數的值
                int type = receivedText.startsWith("收入報表") ? 0 : 1;

                // 根據使用者輸入更換時間範圍
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
                    timePeriod = "month"; // 預設為 "本月"
                }

                String outputFilePath = "src/main/resources/static/images/transactionPieChart.png";
                // 生成圓餅圖
                String imagePath = chartGenerateService.generatePieChart(type, timePeriod, outputFilePath, userId);

                if (imagePath != null) {
                    log.info("圖片生成並儲存成功，路徑為: " + imagePath);
                } else {
                    log.info("圖片生成失敗");
                }

                // 根據開頭動態設置 displayText
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

                String s3Key = "images/transaction/" + UUID.randomUUID() + ".png"; // 在 S3 上的路徑和檔案名稱
                String imageUrl = uploadImageToS3(imagePath, s3Key);

//                flexMessageJsonObject.getJSONObject("hero")
//                        .getJSONObject("action")
//                        .put("uri", imageUrl);

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

            else if (receivedText != null && receivedText.matches("\\$.*")) {
                try {
                    Map<String, Object> expenseCategoryMap = categoryService.get(1, "all", userId);
                    List<UpdateCategoryDto> expenseCategories = (List<UpdateCategoryDto>) expenseCategoryMap.get("data");

                    // 動態生成 FlexMessage 的分類按鈕 JSON 內容
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

                    // 解析 JSON 並發送 FlexMessage
                    FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);
                    FlexMessage flexMessage = new FlexMessage("記支出", flexContainer);

                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    sendLineMessage(userId, "記帳失敗");
                }
            }

            else if (receivedText != null && receivedText.matches("\\+.*")) {
                try {
                    Map<String, Object> incomeCategoryMap = categoryService.get(0, "all", userId);
                    List<UpdateCategoryDto> incomeCategories = (List<UpdateCategoryDto>) incomeCategoryMap.get("data");

                    // 動態生成 FlexMessage 的分類按鈕 JSON 內容
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

                    // 解析 JSON 並發送 FlexMessage
                    FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);
                    FlexMessage flexMessage = new FlexMessage("記收入", flexContainer);

                    lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    sendLineMessage(userId, "記帳失敗");
                }
            }

            else if ( receivedText != null && receivedText.matches("^刪除.*")){
                // 解析輸入以取得類別和 ID
                String[] parts = receivedText.split(":");
                if (parts.length == 2) {
                    String type = receivedText.substring(2);
                    int transactionId = Integer.parseInt(parts[1].trim());

                    // 呼叫 transactionService 刪除該筆資料
                    transactionService.delete(transactionId);

                    // 回覆刪除成功訊息
                    sendLineMessage(userId, type + " 已成功刪除");
                }
            }

            else if (receivedText != null && receivedText.matches("^本月結餘.*")) {

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
                // 生成長條圖
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

                String s3Key = "images/balance/" + UUID.randomUUID() + ".png"; // 在 S3 上的路徑和檔案名稱
                String imageUrl = uploadImageToS3(imagePath, s3Key);

//                flexMessageJsonObject.getJSONObject("hero")
//                        .getJSONObject("action")
//                        .put("uri", imageUrl);

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

            else if (receivedText != null && receivedText.matches("^匯出上月報表.*")) {

                ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
                LocalDate today = LocalDate.now(taipeiZone); // 取得台灣時間的今天日期
                LocalDate startDate = today.minusMonths(1).withDayOfMonth(1);
                LocalDate endDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

                String startDateStr = dateToString(startDate);
                String endDateStr = dateToString(endDate);

                String message = s3Service.exportCsvFile(userId, transactionService.getAllTransaction(startDateStr, endDateStr, userId));

                TextMessage textMessage = new TextMessage("請點擊網址下載上個月報表的 CSV 檔"+ "\n" + message);

                lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

            }

            else if (receivedText != null && receivedText.matches("^分析上月報表.*")) {

                Map<String, Object> expense = transactionService.getTransaction(1, "all", "lastMonth", userId);
                Map<String, Object> income = transactionService.getTransaction(0, "all", "lastMonth", userId);

                Map<String, Object> response = aiService.getFeedback(expense, income);

                // 取得 "data" 的內容
                AIFeedbackDto data = (AIFeedbackDto) response.get("data");

                // 分別提取分析和建議訊息
                String analysisMessage = data.getAnalysis();
                String adviceMessage = data.getAdvice();

                TextMessage textMessage = new TextMessage("理財分析:"+ "\n" + analysisMessage + "\n\n" + "理財建議:"+ "\n" + adviceMessage);
                lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

            }

            else if (receivedText != null && (receivedText.matches("^指令.*") || receivedText.matches("^教學.*"))) {
                String command = "使用教學如下 : \n" +
                        "\n" +
                        "1.記支出\n" +
                        "輸入: $(金額)(空格)(備註)\n" +
                        "ex. $100 午餐\n" +
                        "(目前分類皆為預設，若想調整請至官網>立即記帳>分類管理)\n" +
                        "\n" +
                        "2.記收入\n" +
                        "$(金額)(空格)(備註)\n" +
                        "ex. +100 零用錢\n" +
                        "(目前分類皆為預設，若想調整請至官網>立即記帳>分類管理)\n" +
                        "\n" +
                        "3.查看支出狀況\n" +
                        "輸入: 支出報表\n" +
                        "\n" +
                        "4.查看收入狀況\n" +
                        "輸入: 收入報表\n" +
                        "\n" +
                        "5.查看結餘\n" +
                        "輸入: 本月結餘\n" +
                        "\n" +
                        "6.匯出成 CSV 檔\n" +
                        "輸入: 匯出上月報表\n" +
                        "\n" +
                        "7.理財分析與建議\n" +
                        "輸入: 分析上月報表\n" +
                        "\n" +
                        "8.查詢指令\n" +
                        "輸入指令或教學皆可\n" +
                        "輸入: 指令\n" +
                        "輸入: 教學\n" +
                        "\n" +
                        "點擊圖表內圖片即可前往官網~";
                sendLineMessage(userId, command);
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

    //記帳，flexMessage 內放 postback
    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String userId = event.getSource().getUserId();
        String postbackData = event.getPostbackContent().getData();

        Integer type = -1;

        try {
            Map<String, Object> expenseCategoryMap = categoryService.get(1, "all", userId);
            Map<String, Object> incomeCategoryMap = categoryService.get(0, "all", userId);

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

            // Check if the postbackData is in the map
            if (categoryMessages.containsKey(postbackData)) {
                type = categoryTypes.get(postbackData);
                String typeStr = "";

                lastUserMessage = redisUtil.getDataFromCache(CACHE_KEY + userId);

                // 預處理輸入字串：去除首尾空白並將多個空白字符替換為單個空格
                String cleanedMessage = lastUserMessage.trim().replaceAll("\\s+", " ");

                // 以空格分割字串，限制最多分成兩部分：金額和描述
                String[] parts = cleanedMessage.substring(1).trim().split("\\s+", 2);

                String amountStr = parts[0].trim();
                int amount = Integer.parseInt(amountStr);

                String description = parts.length > 1 ? parts[1].trim() : "";

                ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
                LocalDate today = LocalDate.now(taipeiZone);
                String todayStr = transactionService.dateToString(today);

                Integer transactionId = transactionService.createTransaction(type, postbackData, amount, description, todayStr, userId);

                if (type == 1) {
                    budgetAlert(postbackData, userId, type);
                }

                // 使用 generateRecordImage 方法生成圖像
                String imagePath = chartGenerateService.generateRecordImage(type, amount, postbackData, userId);

                if (imagePath == null) {
                    sendLineMessage(userId, "記帳失敗，無法生成圖像");
                    return;
                }

                // 上傳圖片到 S3
                String s3Key = "images/record/" + UUID.randomUUID() + ".png"; // 在 S3 上的路徑和檔案名稱
                String imageUrl = uploadImageToS3(imagePath, s3Key);

                if (imageUrl == null) {
                    sendLineMessage(userId, "記帳失敗，無法上傳圖片至 S3");
                    return;
                }

                if(type.equals(0)){
                    typeStr = "收入";
                }else if(type.equals(1)){
                    typeStr = "支出";
                }

                // 動態生成 FlexMessage 的 JSON 內容
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

// 解析 JSON 並發送 FlexMessage
                FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);
                FlexMessage flexMessage = new FlexMessage("記帳資訊", flexContainer);

                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢復中斷狀態
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

            if(totalCost / budgetPrice >= 1){
                sendLineMessage(lineUserId, "已超出 " + category + " 預算");
            } else if ((double)totalCost / budgetPrice >= 0.5) {
                sendLineMessage(lineUserId, "已超出 " + category + " 一半預算");
            } else {
                //TODO 未超出一半預算
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
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        LocalDate today = LocalDate.now(taipeiZone); // 取得台灣時間的今天日期
        // 定義日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 將 LocalDate 轉換為 String
        String dateStr = date.format(formatter);
        return dateStr;
    }

}