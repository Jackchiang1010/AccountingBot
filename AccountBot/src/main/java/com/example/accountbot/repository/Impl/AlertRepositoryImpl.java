package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.alert.AlertDto;
import com.example.accountbot.dto.alert.GetAlertDto;
import com.example.accountbot.dto.alert.UpdateAlertDto;
import com.example.accountbot.repository.AlertRepository;
import com.example.accountbot.rowmapper.alert.GetAlertRowMapper;
import com.example.accountbot.rowmapper.alert.UpdateAlertRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AlertRepositoryImpl implements AlertRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer create(AlertDto alertDto) {
        String sql = "INSERT INTO alert(time, description, lineuser_id) VALUES (:time, :description, :lineuser_id);";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("time", alertDto.getTime());
        map.put("description", alertDto.getDescription());
        map.put("lineuser_id", alertDto.getLineUserId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

            Integer alertId = keyHolder.getKey().intValue();

            return alertId;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            throw new RuntimeException("Failed to create alert", e);
        }
    }

    @Override
    public List<GetAlertDto> get(String lineUserId) {
        String sql = "SELECT * FROM alert WHERE lineuser_id = :lineUserId;";

        Map<String, Object> map = new HashMap<>();
        map.put("lineUserId", lineUserId);

        try {
            return namedParameterJdbcTemplate.query(sql, map, new GetAlertRowMapper());
        }catch (DataAccessException e){

            log.info("error : " + e.getMessage());

            throw new RuntimeException("Failed to get alert", e);
        }
    }

    @Override
    public UpdateAlertDto update(UpdateAlertDto updateAlertDto) {
        try {

            String updateSql = "UPDATE alert SET time = :time, description = :description " +
                    "WHERE lineuser_id = :lineuser_id AND id = :id;";

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", updateAlertDto.getId());
            map.put("time", updateAlertDto.getTime());
            map.put("description", updateAlertDto.getDescription());
            map.put("lineuser_id", updateAlertDto.getLineUserId());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            namedParameterJdbcTemplate.update(updateSql, new MapSqlParameterSource(map), keyHolder);

            String selectSql = "SELECT * FROM alert WHERE id = :id;";
            Map<String, Object> selectMap = new HashMap<String, Object>();
            selectMap.put("id", updateAlertDto.getId());

            UpdateAlertDto updateAlert = namedParameterJdbcTemplate.queryForObject(selectSql, selectMap, new UpdateAlertRowMapper());

            return updateAlert;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update alert", e);
        }
    }
}
