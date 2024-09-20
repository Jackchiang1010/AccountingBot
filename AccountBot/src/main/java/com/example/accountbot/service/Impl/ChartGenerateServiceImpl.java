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
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGenerateServiceImpl implements ChartGenerateService {

    private final TransactionService transactionService;

    @Override
    public String generateBarChart(Integer income, Integer expense, Integer balance, String outputFilePath) {
        int width = 600;
        int height = 300;
        int barWidth = 100;
        int barSpacing = 60;

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

            g2d.setColor(Color.decode("#F8E8C9"));
            g2d.fillRoundRect(0, 0, width, height, 40, 40);

            // 設置長條圖的起始位置
            int startX = (width - (barWidth * 3 + barSpacing * 2)) / 2; // 確保長條圖在圖片中間
            int baseY = height - 50; // 長條圖的底部位置

            int maxAmount = Math.max(income, Math.max(expense, balance));
            int expenseBarHeight = (int) ((double) expense / maxAmount * (height - 100));
            int incomeBarHeight = (int) ((double) income / maxAmount * (height - 100));
            int balanceBarHeight = (int) ((double) balance / maxAmount * (height - 100));

            // 繪製支出長條
            g2d.setColor(Color.decode("#E97777"));
            g2d.fillRoundRect(startX, baseY - expenseBarHeight, barWidth, expenseBarHeight, 20, 20);
            g2d.setColor(Color.decode("#FF8B8B"));
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));

            g2d.setColor(Color.decode("#E97777")); // 文字顏色
            String expenseLabel = "支出\n$" + expense;
            int labelWidth = g2d.getFontMetrics().stringWidth(expenseLabel);
            g2d.drawString(expenseLabel, startX + (barWidth - labelWidth) / 2, baseY + 20); // 置中顯示

            startX += barWidth + barSpacing;

            // 繪製收入長條
            g2d.setColor(Color.decode("#52A26E"));
            g2d.fillRoundRect(startX, baseY - incomeBarHeight, barWidth, incomeBarHeight, 20, 20);
            g2d.setColor(Color.decode("#48A05F"));

            String incomeLabel = "收入\n$" + income;
            labelWidth = g2d.getFontMetrics().stringWidth(incomeLabel);
            g2d.drawString(incomeLabel, startX + (barWidth - labelWidth) / 2, baseY + 20); // 置中顯示

            startX += barWidth + barSpacing;

            // 繪製結餘長條
            g2d.setColor(Color.decode("#FFB547"));
            g2d.fillRoundRect(startX, baseY - balanceBarHeight, barWidth, balanceBarHeight, 20, 20);
            g2d.setColor(Color.decode("#F6A83D"));

            String balanceLabel = "結餘\n$" + balance;
            labelWidth = g2d.getFontMetrics().stringWidth(balanceLabel);
            g2d.drawString(balanceLabel, startX + (barWidth - labelWidth) / 2, baseY + 20); // 置中顯示

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

            // 背景色
            g2d.setColor(new Color(255, 253, 234)); // 黃色背景
            g2d.fillRect(0, 0, width, height);

            // 設置反鋸齒
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 繪製圓環
            int centerX = width / 3;
            int centerY = height / 2;
            int outerRadius = 150;
            int innerRadius = 100;

            // 從 transactionService 獲取數據
            Map<String, Object> transactionData = transactionService.getTransaction(type, "all", time, lineUserId);
            List<CategoryCostDto> transactions = (List<CategoryCostDto>) transactionData.get("data");

            // 固定顏色
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
                labels[i] = transactions.get(i).getCategory();
            }

            // 計算總和
            double total = 0;
            for (double value : values) {
                total += value;
            }

            // 繪製圓環
            int startAngle = 0;

            for (int i = 0; i < values.length; i++) {
                int angle = (int) Math.round((values[i] / total) * 360);
                g2d.setColor(colors[i % colors.length]);
                g2d.fillArc(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2, startAngle, angle);
                startAngle += angle;
            }

            // 繪製內圓
            g2d.setColor(new Color(255, 253, 234));
            g2d.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);

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
                g2d.drawString(labels[i] + " " + amount + " (" + (int) (values[i] / total * 100) + "%)",
                        legendX + squareSize + 10, legendY + i * legendSpacing + squareSize / 2 + 5);
            }

            // 繪製合計文字
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
            g2d.drawString("合計", centerX - 20, centerY - 20);
            g2d.drawString("$" + (int) total, centerX - 30, centerY + 20);

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

}
