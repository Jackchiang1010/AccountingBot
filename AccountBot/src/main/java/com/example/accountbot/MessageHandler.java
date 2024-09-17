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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;

@LineMessageHandler
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final TransactionService transactionService;

    @Value("${aws.accessKey}")
    String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private String lastUserMessage = "";

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.info("event: " + event);
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws ExecutionException, InterruptedException, IOException {
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

                FlexMessage flexMessage = new FlexMessage("記支出", flexContainer);

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

                FlexMessage flexMessage = new FlexMessage("記收入", flexContainer);

                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 如果用戶輸入以 "本月結餘" 開頭
        if (receivedText != null && receivedText.matches("^本月結餘.*")) {

            BalanceDto balanceDto = transactionService.balance();
            int totalIncome = balanceDto.getTotalIncome();
            int totalExpenses = balanceDto.getTotalExpenses();
            int netBalance = totalIncome - totalExpenses;

            String message = String.format(
                    "本月結餘：收入(%d) - 支出(%d) = %d 元\n\n累積資產：%d 元",
                    totalIncome, totalExpenses, netBalance, netBalance
            );

            TextMessage textMessage = new TextMessage(message);

            String outputFilePath = "src/main/resources/static/images/chart.png";
            // 生成長條圖
            String imagePath = BarChartGenerator.generateBarChart(totalIncome, totalExpenses, netBalance, outputFilePath);

            if (imagePath != null) {
                log.info("圖片生成並儲存成功，路徑為: " + imagePath);
            } else {
                log.info("圖片生成失敗");
            }

            lineMessagingClient.pushMessage(new PushMessage(userId, textMessage)).get();

            String flexMessageJson = """
            {
                 "type": "bubble",
                 "hero": {
                   "type": "image",
                   "size": "full",
                   "aspectMode": "fit",
                   "action": {
                     "type": "uri",
                     "uri": "https://line.me/"
                   },
                   "url": "%s.png",
                   "aspectRatio": "2:1"
                 }
               }
        """;

            JSONObject flexMessageJsonObject = new JSONObject(flexMessageJson);

//            String baseUrl = "http://localhost:8080"; // 如果是部署在伺服器上，請使用你的網域名稱
//            String imageUrl = baseUrl + "/images/chart.png";
//            log.info("imageUrl : " + imageUrl);

            String filePath = imagePath; // 本地圖片路徑
            String uuid = UUID.randomUUID().toString();
            String s3Key = "images/balance_" + uuid + ".png"; // 在 S3 上的路徑和檔案名稱
            String imageUrl = uploadImageToS3AndSendToLineBot(filePath, s3Key);

            log.info("imageUrl : " + imageUrl);

            flexMessageJsonObject.getJSONObject("hero")
//                    .put("url", "https://accountingbot.s3.ap-northeast-1.amazonaws.com/images/chart.png");
//                    .put("url", imagePath);
                    .put("url", imageUrl);

            String updatedFlexMessageJson = flexMessageJsonObject.toString();

            try {

                FlexContainer flexContainer = objectMapper.readValue(updatedFlexMessageJson, FlexContainer.class);

                FlexMessage flexMessage = new FlexMessage("本月結餘", flexContainer);

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

    public class BarChartGenerator {

        public static String generateBarChart(int income, int expense, int balance, String outputFilePath) throws IOException {
            int width = 400;
            int height = 200;

            try {
                // 取得檔案儲存目錄
                File outputFile = new File(outputFilePath);
                File outputDir = outputFile.getParentFile();

                // 檢查並建立目錄
                if (!outputDir.exists()) {
                    boolean dirCreated = outputDir.mkdirs();
                    if (dirCreated) {
                        log.info("目錄建立成功: " + outputDir.getAbsolutePath());
                    } else {
                        log.info("目錄建立失敗: " + outputDir.getAbsolutePath());
                    }
                } else {
                    log.info("目錄已存在: " + outputDir.getAbsolutePath());
                }

                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();

                // 背景顏色
                g2d.setColor(Color.decode("#F8E8C9"));
                g2d.fillRoundRect(0, 0, width, height, 40, 40);

                // 繪製長條圖背景
                g2d.setColor(Color.decode("#FDEEDC"));
                g2d.fillRoundRect(50, 50, 300, 30, 20, 20);

                // 計算長條的比例
                int total = income;
                int expenseBarWidth = (int) ((double) expense / total * 300);

                // 繪製支出長條
                g2d.setColor(Color.decode("#E97777"));
//                g2d.fillOval(50 + expenseBarWidth - 15, 40, 30, 30);
                g2d.fillRoundRect(50, 50, expenseBarWidth, 30, 20, 20);

                // 畫雞的圖案 (簡化為一個圓形)
//                g2d.setColor(Color.WHITE);
//                g2d.fillOval(expenseBarWidth, 50, 30, 30);

                // 添加文字
                g2d.setColor(Color.decode("#FF8B8B"));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2d.drawString("支出", 50, 120);
                g2d.drawString("$" + expense, 50, 150);

                g2d.setColor(Color.decode("#917FB3"));
                g2d.drawString("目前結餘", 160, 120);
                g2d.drawString("$" + balance, 160, 150);

                g2d.setColor(Color.decode("#52A26E"));
                g2d.drawString("收入", 300, 120);
                g2d.drawString("$" + income, 300, 150);

                // 釋放圖形資源
                g2d.dispose();

                // 輸出圖片
                ImageIO.write(bufferedImage, "png", outputFile);
                log.info("圖片已成功儲存到: " + outputFile.getAbsolutePath());

                return outputFile.getAbsolutePath();

            } catch (IOException e) {
                log.info("生成圖片時發生錯誤: " + e.getMessage());
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                log.info("發生未知錯誤: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }

    public class S3Uploader {
        private AmazonS3 s3Client;

        public S3Uploader(String accessKey, String secretKey) {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
//                    .withRegion("ap-northeast-1")
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();
        }

        public String uploadFile(String filePath, String s3Key) {
            File file = new File(filePath);
            log.info("filePath" + filePath);
            s3Client.putObject(new PutObjectRequest(bucketName, s3Key, file));
            return s3Client.getUrl(bucketName, s3Key).toString();
        }
    }

    public String uploadImageToS3AndSendToLineBot(String filePath, String s3Key) {


        // 建立 S3 上傳器
        S3Uploader s3Uploader = new S3Uploader(accessKey, secretKey);

        // 上傳圖片並取得 S3 上的 URL
        String imageUrl = s3Uploader.uploadFile(filePath, s3Key);
        log.info("Image uploaded to S3, URL: " + imageUrl);

        return imageUrl;
    }


}