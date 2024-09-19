package com.example.accountbot.repository;

import com.example.accountbot.dto.alert.AlertDto;
import com.example.accountbot.dto.alert.GetAlertDto;
import com.example.accountbot.dto.alert.UpdateAlertDto;

import java.util.List;

public interface AlertRepository {

    Integer create(AlertDto alertDto);

    List<GetAlertDto> get(String lineUserId);

    UpdateAlertDto update(UpdateAlertDto updateAlertDto);

    boolean delete(Integer id);

    List<UpdateAlertDto> getAllAlerts();

}
