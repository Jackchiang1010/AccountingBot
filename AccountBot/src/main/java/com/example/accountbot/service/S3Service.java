package com.example.accountbot.service;

import com.example.accountbot.dto.transaction.GetAllTransactionDto;

import java.util.List;

public interface S3Service {

    String uploadFile(String filePath, String s3Key);

    String exportCsvFile(String userId, List<GetAllTransactionDto> reportList);

}
