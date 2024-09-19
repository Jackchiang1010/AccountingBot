package com.example.accountbot.service.Impl;

import com.example.accountbot.service.ChartGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGenerateServiceImpl implements ChartGenerateService {

    public String generateBarChart(int income, int expense, int balance, String outputFilePath) {
        int width = 600;  // 圖片寬度
        int height = 300; // 圖片高度
        int barWidth = 100; // 每個長條圖的寬度
        int barSpacing = 60; // 每個長條圖之間的間隔
        int labelSpacing = 20; // 標籤與長條圖之間的間隔

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

            // 設置長條圖的起始位置
            int startX = (width - (barWidth * 3 + barSpacing * 2)) / 2; // 確保長條圖在圖片中間
            int baseY = height - 50; // 長條圖的底部位置

            // 計算長條的高度
            int maxAmount = Math.max(income, Math.max(expense, balance));  // 最大數值
            int expenseBarHeight = (int) ((double) expense / maxAmount * (height - 100));
            int incomeBarHeight = (int) ((double) income / maxAmount * (height - 100));
            int balanceBarHeight = (int) ((double) balance / maxAmount * (height - 100));

            // 繪製支出長條
            g2d.setColor(Color.decode("#E97777"));
            g2d.fillRoundRect(startX, baseY - expenseBarHeight, barWidth, expenseBarHeight, 20, 20);
            g2d.setColor(Color.decode("#FF8B8B"));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
            // 繪製支出標籤
            g2d.setColor(Color.decode("#E97777")); // 文字顏色
            String expenseLabel = "支出\n$" + expense;
            int labelWidth = g2d.getFontMetrics().stringWidth(expenseLabel);
            g2d.drawString(expenseLabel, startX + (barWidth - labelWidth) / 2, baseY + 20); // 置中顯示

            startX += barWidth + barSpacing;

            // 繪製收入長條
            g2d.setColor(Color.decode("#52A26E"));
            g2d.fillRoundRect(startX, baseY - incomeBarHeight, barWidth, incomeBarHeight, 20, 20);
            g2d.setColor(Color.decode("#48A05F"));
            // 繪製收入標籤
            String incomeLabel = "收入\n$" + income;
            labelWidth = g2d.getFontMetrics().stringWidth(incomeLabel);
            g2d.drawString(incomeLabel, startX + (barWidth - labelWidth) / 2, baseY + 20); // 置中顯示

            startX += barWidth + barSpacing;

            // 繪製結餘長條
            g2d.setColor(Color.decode("#FFB547"));
            g2d.fillRoundRect(startX, baseY - balanceBarHeight, barWidth, balanceBarHeight, 20, 20);
            g2d.setColor(Color.decode("#F6A83D"));
            // 繪製結餘標籤
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

}
