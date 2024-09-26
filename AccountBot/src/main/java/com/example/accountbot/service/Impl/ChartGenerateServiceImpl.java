package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.service.ChartGenerateService;
import com.example.accountbot.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGenerateServiceImpl implements ChartGenerateService {

    private final TransactionService transactionService;

    @Override
    public String generateBarChart(Integer income, Integer expense, Integer balance, String outputFilePath) {
        int width = 1200;
        int height = 600;
        int barWidth = 150;
        int barSpacing = 120;

        try {

            File outputFile = new File(outputFilePath);
            File outputDir = outputFile.getParentFile();

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

            g2d.setColor(new Color(255, 253, 234));
            g2d.fillRoundRect(0, 0, width, height, 40, 40);

            // 設置長條圖的起始位置
            int startX = (width - (barWidth * 3 + barSpacing * 2)) / 2; // 確保長條圖在圖片中間
            int baseY = height - 100; // 調整長條圖的底部位置

            int maxAmount = Math.max(income, Math.max(expense, balance));
            int expenseBarHeight = (int) ((double) expense / maxAmount * (height - 150));
            int incomeBarHeight = (int) ((double) income / maxAmount * (height - 150));
            int balanceBarHeight = (int) ((double) balance / maxAmount * (height - 150));

            // 繪製支出長條
            g2d.setColor(new Color(242, 158, 142));
            g2d.fillRoundRect(startX, baseY - expenseBarHeight, barWidth, expenseBarHeight, 20, 20);
            g2d.setColor(new Color(242, 158, 142));
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));

//            g2d.setColor(new Color(0, 0, 0));
            String expenseLabel = "支出\n$" + expense;
            int labelWidth = g2d.getFontMetrics().stringWidth(expenseLabel);
            g2d.drawString(expenseLabel, startX + (barWidth - labelWidth) / 2, baseY + 30);

            startX += barWidth + barSpacing;

            // 繪製收入長條
            g2d.setColor(new Color(173, 220, 147));
            g2d.fillRoundRect(startX, baseY - incomeBarHeight, barWidth, incomeBarHeight, 20, 20);
            g2d.setColor(new Color(173, 220, 147));

//            g2d.setColor(new Color(0, 0, 0));
            String incomeLabel = "收入\n$" + income;
            labelWidth = g2d.getFontMetrics().stringWidth(incomeLabel);
            g2d.drawString(incomeLabel, startX + (barWidth - labelWidth) / 2, baseY + 30);

            startX += barWidth + barSpacing;

            // 繪製結餘長條
            g2d.setColor(new Color(249, 213, 138));
            g2d.fillRoundRect(startX, baseY - balanceBarHeight, barWidth, balanceBarHeight, 20, 20);
            g2d.setColor(new Color(249, 213, 138));

//            g2d.setColor(new Color(0, 0, 0));
            String balanceLabel = "結餘\n$" + balance;
            labelWidth = g2d.getFontMetrics().stringWidth(balanceLabel);
            g2d.drawString(balanceLabel, startX + (barWidth - labelWidth) / 2, baseY + 30);

            // 釋放圖形資源
            g2d.dispose();

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

    @Override
    public String generatePieChart(Integer type, String time, String outputFilePath, String lineUserId) {
        int width = 500;
        int height = 400;

        try {
            File outputFile = new File(outputFilePath);
            File outputDir = outputFile.getParentFile();

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

            // 背景色
            g2d.setColor(new Color(255, 253, 234));
            g2d.fillRect(0, 0, width, height);

            // 設置反鋸齒
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 繪製圓環
            int centerX = width / 3;
            int centerY = height / 2;
            int outerRadius = 150;
            int innerRadius = 100;

            Map<String, Object> transactionData = transactionService.getTransaction(type, "all", time, lineUserId);
            List<CategoryCostDto> transactions = (List<CategoryCostDto>) transactionData.get("data");

            Color[] colors = {
                    new Color(242, 158, 142),
                    new Color(249, 213, 138),
                    new Color(246, 229, 127),
                    new Color(173, 220, 147),
                    new Color(140, 205, 198),
                    new Color(170, 180, 227),
                    new Color(204, 188, 219)
            };

            // 各類收入數據
            double[] values = new double[transactions.size()];
            String[] labels = new String[transactions.size()];

            for (int i = 0; i < transactions.size(); i++) {
                values[i] = transactions.get(i).getTotalCost();
                log.info("values[i] : " + values[i]);
                labels[i] = transactions.get(i).getCategory();
            }

            // 計算總和
            double total = 0;
            for (double value : values) {
                total += value;
            }

            // 繪製圓環
            int startAngle = 0; // 起始角度
            double textAngle = 0; // 起始角度

            for (int i = 0; i < values.length; i++) {
                // 確保角度計算正確
                int angle = (int) Math.round((values[i] / total) * 360);
                log.info("angle : " + angle);

                // 繪製圓環
                g2d.setColor(colors[i % colors.length]);
                g2d.fillArc(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2, startAngle, angle);

                // 計算文字應該顯示的角度
                double radian = Math.toRadians(textAngle - angle / 2.0);

                // 計算文字的位置
                int textX = (int) (centerX + (outerRadius + innerRadius) / 2 * Math.cos(radian));
                int textY = (int) (centerY + (outerRadius + innerRadius) / 2 * Math.sin(radian));

                // 繪製百分比文字
                g2d.setColor(Color.BLACK);
                String percentageText = (int) (values[i] / total * 100) + "%";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(percentageText);
                int textHeight = fm.getAscent();

                // 將文字位置調整到圓環的中心
                g2d.drawString(percentageText, textX - textWidth / 2, textY + textHeight / 4); // 文字居中

                // 更新起始角度
                textAngle -= angle; // 更新起始角度以供下次使用
                log.info("textAngle : " + textAngle);

                // 更新起始角度
                startAngle += angle; // 更新起始角度以供下次使用
            }

            // 繪製內圓
            g2d.setColor(new Color(255, 253, 234));
            g2d.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);

            // 繪製時間區段
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            g2d.setColor(Color.BLACK);
            String timePeriodText = getTimePeriodText(time);
            FontMetrics timeFm = g2d.getFontMetrics();
            int timeTextWidth = timeFm.stringWidth(timePeriodText);
            int timeTextX = (width - timeTextWidth) / 2;
            g2d.drawString(timePeriodText, timeTextX, 30);

            // 繪製分類標籤，包括金額
            int legendX = centerX + outerRadius + 20;
            int legendY = centerY - outerRadius;
            int legendSpacing = 35;
            int squareSize = 30;

            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));

            for (int i = 0; i < values.length; i++) {
                g2d.setColor(colors[i % colors.length]);
                g2d.fillRect(legendX, legendY + i * legendSpacing, squareSize, squareSize);
                g2d.setColor(Color.BLACK);

                String amount = values[i] > 0 ? "$" + (int) values[i] : "$0";
                g2d.drawString(labels[i] + " " + amount, legendX + squareSize + 10, legendY + i * legendSpacing + squareSize / 2 + 5);
            }

            String totalText = "合計";
            String amountText = "$" + (int) total;

            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));

            FontMetrics fontMetrics = g2d.getFontMetrics();
            int totalTextWidth = fontMetrics.stringWidth(totalText);
            int amountTextWidth = fontMetrics.stringWidth(amountText);
            int textHeight = fontMetrics.getAscent();

            int totalTextX = centerX - totalTextWidth / 2;
            int totalTextY = centerY - textHeight / 2;

            int amountTextX = centerX - amountTextWidth / 2;
            int amountTextY = centerY + textHeight;

            g2d.setColor(Color.BLACK);
            g2d.drawString(totalText, totalTextX, totalTextY);
            g2d.drawString(amountText, amountTextX, amountTextY);

            // 釋放圖形資源
            g2d.dispose();

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

    private String getTimePeriodText(String time) {
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        LocalDate today = LocalDate.now(taipeiZone); // 取得台灣時間的今天日期
        LocalDate startDate = null;
        LocalDate endDate = today; // 預設結束時間為今天

        switch (time) {
            case "yesterday":
                startDate = endDate.minusDays(1);
                break;
            case "week":
                // 當前週的禮拜一
                startDate = endDate.with(DayOfWeek.MONDAY);
                break;
            case "month":
                // 當前月份的開始日期
                startDate = endDate.withDayOfMonth(1);
                break;
            case "halfYear":
                startDate = endDate.minusMonths(6).withDayOfMonth(1); // 包含這個半年的第一天
                break;
            default:
                startDate = endDate.withDayOfMonth(1); // 預設為當前月份的第一天
                break;
        }

        // 格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return startDate.format(formatter) + " - " + endDate.format(formatter); // 返回日期範圍字符串
    }

    @Override
    public String generateRecordImage(Integer type, Integer amount, String category, String lineUserId) {
        int width = 400; // 調整寬度以適應 Line Flex Message
        int height = 200; // 調整高度以避免被切割

        try {
            // 設定圖片的輸出路徑
            String outputFilePath = "src/main/resources/static/images/transactionRecord.png"; // 修改為您的輸出路徑
            File outputFile = new File(outputFilePath);
            File outputDir = outputFile.getParentFile();

            if (!outputDir.exists() && !outputDir.mkdirs()) {
                log.info("目錄建立失敗: " + outputDir.getAbsolutePath());
                return null;
            }

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();

            // 背景設定
            g2d.setColor(new Color(255, 253, 234)); // 以附圖相似的背景顏色
            g2d.fillRoundRect(0, 0, width, height, 40, 40);

            // 開啟抗鋸齒
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 設定字型
            Font font = new Font("Microsoft JhengHei", Font.PLAIN, 24);
            g2d.setFont(font);
            g2d.setColor(new Color(0, 0, 0)); // 黑色字體

            // 取得交易資料
            Map<String, Object> transactionData = transactionService.getTransaction(type, category, "month", lineUserId);
            List<CategoryCostDto> dataList = (List<CategoryCostDto>) transactionData.get("data");

            int totalCost = 0;
            for (CategoryCostDto data : dataList) {
                if (data.getCategory().equals(category)) {
                    totalCost = data.getTotalCost();
                    break;
                }
            }

            // 設定起始位置
            int paddingLeft = 40;
            int firstLineY = 70; // 第一行Y軸位置
            int secondLineY = 140; // 第二行Y軸位置

            // 第一行：已將 100 元新增至 飲食
            int currentX = paddingLeft;
            g2d.drawString("已將", currentX, firstLineY);

            // 繪製金額
            currentX += g2d.getFontMetrics().stringWidth("已將 ") + 10;
            g2d.drawString(amount.toString(), currentX, firstLineY);
            int amountWidth = g2d.getFontMetrics().stringWidth(amount.toString());
            g2d.drawLine(currentX - 5, firstLineY + 5, currentX + amountWidth + 5, firstLineY + 5);

            // 元新增至
            currentX += amountWidth + 20;
            g2d.drawString("元新增至", currentX, firstLineY);

            // 繪製分類
            currentX += g2d.getFontMetrics().stringWidth("元新增至 ") + 10;
            g2d.drawString(category, currentX, firstLineY);
            int categoryWidth = g2d.getFontMetrics().stringWidth(category);
            g2d.drawLine(currentX - 5, firstLineY + 5, currentX + categoryWidth + 5, firstLineY + 5);

            // 第二行：本月 飲食 已累積 100 元
            currentX = paddingLeft;
            g2d.drawString("本月", currentX, secondLineY);

            // 繪製分類
            currentX += g2d.getFontMetrics().stringWidth("本月 ") + 10;
            g2d.drawString(category, currentX, secondLineY);
            g2d.drawLine(currentX - 5, secondLineY + 5, currentX + categoryWidth + 5, secondLineY + 5);

            // 已累積
            currentX += categoryWidth + 20;
            g2d.drawString("已累積", currentX, secondLineY);

            // 繪製總花費
            currentX += g2d.getFontMetrics().stringWidth("已累積 ") + 10;
            g2d.drawString(String.valueOf(totalCost), currentX, secondLineY);
            int totalCostWidth = g2d.getFontMetrics().stringWidth(String.valueOf(totalCost));
            g2d.drawLine(currentX - 5, secondLineY + 5, currentX + totalCostWidth + 5, secondLineY + 5);

            // 元
            currentX += totalCostWidth + 10;
            g2d.drawString("元", currentX, secondLineY);

            // 釋放圖形資源
            g2d.dispose();

            // 保存圖片
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
