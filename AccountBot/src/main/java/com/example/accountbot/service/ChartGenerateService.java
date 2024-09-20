package com.example.accountbot.service;

public interface ChartGenerateService {

    String generateBarChart(Integer income, Integer expense, Integer balance, String outputFilePath);

    String generatePieChart(Integer type, String time, String outputFilePath, String lineUserId);

}
