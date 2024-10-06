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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new HashMap<>();


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

        clearAllScheduledTasks(); // 先清除之前的所有排程
        scheduleAllAlerts(); // 再重新排程所有提醒

        Map<String, Object> result = new HashMap<>();
        result.put("data", updatedCategoryDto);

        return result;
    }


    @Override
    public boolean delete(Integer id) {
        // 取消該提醒的排程
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(id);
        if (scheduledTask != null) {
            scheduledTask.cancel(false); // false 表示不會中斷正在執行的任務
        }

        return alertRepository.delete(id);
    }

    @PostConstruct
    public void initializeAlerts() {
        // 清空所有排程
        clearAllScheduledTasks();

        scheduleAllAlerts();
    }

    private synchronized void clearAllScheduledTasks() {
        // 遍歷所有已排程的任務，取消它們
        for (ScheduledFuture<?> scheduledTask : scheduledTasks.values()) {
            if (scheduledTask != null) {
                scheduledTask.cancel(false); // 取消所有現有的排程
            }
        }
        // 清空追蹤任務的 Map
        scheduledTasks.clear();
    }

    //每小時更新
    @Scheduled(cron = "0 * * * * ?")
    public void scheduleAllAlerts() {
        List<UpdateAlertDto> alerts = alertRepository.getAllAlerts();
        for (UpdateAlertDto alert : alerts) {
            scheduleAlert(alert);
        }
    }

    private synchronized void scheduleAlert(UpdateAlertDto alert) {
        // 取消之前的相同 ID 的提醒排程
        ScheduledFuture<?> previousTask = scheduledTasks.get(alert.getId());
        if (previousTask != null) {
            previousTask.cancel(false);
        }

        long delay = calculateDelay(LocalTime.parse(alert.getTime()));
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
            sendLineMessage(alert.getLineUserId(), alert.getDescription());
            // 再次排程該提醒，確保每天提醒
            scheduleAlert(alert);
        }, delay, TimeUnit.MILLISECONDS);

        // 儲存該排程的任務，方便之後取消
        scheduledTasks.put(alert.getId(), scheduledTask);
    }


    private long calculateDelay(LocalTime alertTime) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Taipei"));
        LocalDateTime nextAlert = now.with(alertTime);

        if (now.isAfter(nextAlert)) {
            nextAlert = nextAlert.plusDays(1);
        }

        return nextAlert.atZone(ZoneId.of("Asia/Taipei")).toInstant().toEpochMilli()
                - now.atZone(ZoneId.of("Asia/Taipei")).toInstant().toEpochMilli();
    }


    private void sendLineMessage(String lineUserId, String description) {
        TextMessage textMessage = new TextMessage(description);
        PushMessage pushMessage = new PushMessage(lineUserId, textMessage);
        lineMessagingClient.pushMessage(pushMessage);
    }
}
