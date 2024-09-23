package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.alert.AlertDto;
import com.example.accountbot.dto.alert.GetAlertDto;
import com.example.accountbot.dto.alert.UpdateAlertDto;
import com.example.accountbot.repository.AlertRepository;
import com.example.accountbot.service.AlertService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Override
    public Map<String, Object> create(AlertDto alertDto) {

        Integer alertDtoId = alertRepository.create(alertDto);

        Map<String, Object> result = new HashMap<>();
        result.put("data", alertDtoId);

        return result;
    }

    @Override
    public Map<String, Object> get(String lineUserId) {
        List<GetAlertDto> getAlertDto = alertRepository.get(lineUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", getAlertDto);

        return result;
    }

    @Override
    public Map<String, Object> update(UpdateAlertDto updateAlertDto) {
        UpdateAlertDto updatedCategoryDto = alertRepository.update(updateAlertDto);

        scheduleAllAlerts();

        Map<String, Object> result = new HashMap<>();
        result.put("data", updatedCategoryDto);

        return result;
    }

    @Override
    public boolean delete(Integer id) {
        return alertRepository.delete(id);
    }

    @PostConstruct
    public void initializeAlerts() {
        scheduleAllAlerts();
    }

    // 定期重新排程，確保每天都會更新提醒
    @Scheduled(cron = "0 0 0 * * ?")  // 每天午夜重新安排所有提醒
    public void scheduleAllAlerts() {
        List<UpdateAlertDto> alerts = alertRepository.getAllAlerts();
        for (UpdateAlertDto alert : alerts) {
            scheduleAlert(alert);
        }
    }

    private void scheduleAlert(UpdateAlertDto alert) {
        long delay = calculateDelay(LocalTime.parse(alert.getTime()));
        scheduler.schedule(() -> {
            sendLineMessage(alert.getLineUserId(), alert.getDescription());
            // 再次排程該提醒，確保每天提醒
            scheduleAlert(alert);
        }, delay, TimeUnit.MILLISECONDS);
    }

    private long calculateDelay(LocalTime alertTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextAlert = now.with(alertTime);

        // 如果現在時間已經過了今天的提醒時間，設置為明天的該時間
        if (now.isAfter(nextAlert)) {
            nextAlert = nextAlert.plusDays(1);
        }

        return nextAlert.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                - now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void sendLineMessage(String lineUserId, String description) {
        TextMessage textMessage = new TextMessage(description);
        PushMessage pushMessage = new PushMessage(lineUserId, textMessage);
        lineMessagingClient.pushMessage(pushMessage);
    }
}
