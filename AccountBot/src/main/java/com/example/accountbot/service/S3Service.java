package com.example.accountbot.service;

public interface S3Service {

    String uploadFile(String filePath, String s3Key);

}
