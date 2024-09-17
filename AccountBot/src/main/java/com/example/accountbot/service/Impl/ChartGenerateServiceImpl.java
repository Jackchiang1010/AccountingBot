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
            g2d.fillRoundRect(50, 50, expenseBarWidth, 30, 20, 20);

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
