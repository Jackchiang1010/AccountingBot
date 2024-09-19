package com.example.accountbot.service.Impl;

import com.example.accountbot.dto.alert.AlertDto;
import com.example.accountbot.dto.alert.GetAlertDto;
import com.example.accountbot.dto.alert.UpdateAlertDto;
import com.example.accountbot.repository.AlertRepository;
import com.example.accountbot.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

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

        Map<String, Object> result = new HashMap<>();
        result.put("data", updatedCategoryDto);

        return result;
    }
}
