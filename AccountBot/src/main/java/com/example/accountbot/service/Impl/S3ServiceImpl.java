package com.example.accountbot.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.accountbot.dto.transaction.GetAllTransactionDto;
import com.example.accountbot.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private AmazonS3 s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Autowired
    public S3ServiceImpl(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(String filePath, String s3Key) {
        File file = new File(filePath);
        log.info("filePath" + filePath);
        s3Client.putObject(new PutObjectRequest(bucketName, s3Key, file));
        return s3Client.getUrl(bucketName, s3Key).toString();
    }

    @Override
    public String exportCsvFile(String userId, List<GetAllTransactionDto> reportList) {
        // 生成 CSV 資料流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
            // 加入 UTF-8 BOM
            writer.write('\uFEFF');

            // 寫入 CSV 標題
            writer.write("日期,類別,金額,備註,收支\n");

            // 寫入資料
            for (GetAllTransactionDto report : reportList) {

                String type = report.getType() == 1 ? "支出" : "收入";

                writer.write(report.getDate().toString() + ","
                        + report.getCategory() + ","
                        + report.getCost() + ","
                        + report.getDescription() + ","
                        + type + "\n");
            }
            writer.flush(); // 確保所有內容都被寫入
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 設定 S3 的 metadata，指定 Content-Type 和 Content-Encoding
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/csv");
        metadata.setContentEncoding("UTF-8");
        metadata.setContentLength(outputStream.size());

        // 將資料流上傳至 S3
        String fileName = "monthly_report/monthly_report_" + UUID.randomUUID() + ".csv";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
        s3Client.putObject(putObjectRequest);

        // 取得檔案 URL
        String fileUrl = "https://" + bucketName + ".s3.ap-northeast-1.amazonaws.com/" + fileName;

        return fileUrl;
    }

}
