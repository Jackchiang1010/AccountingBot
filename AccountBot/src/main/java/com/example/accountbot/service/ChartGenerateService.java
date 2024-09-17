package com.example.accountbot.service;

public interface ChartGenerateService {

    String generateBarChart(int income, int expense, int balance, String outputFilePath);

}
