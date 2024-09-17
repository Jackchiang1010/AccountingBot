package com.example.accountbot.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.accountbot.service.S3Service;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

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
}
