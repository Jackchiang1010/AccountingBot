package com.example.accountbot.service;

import java.util.Map;

public interface AIService {

    Map<String, Object> getFeedback(Map<String, Object> expense, Map<String, Object> income);

}
