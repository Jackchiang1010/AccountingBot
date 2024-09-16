package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BudgetRepositoryImpl implements BudgetRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer create(BudgetDto budgetDto) {
        String sql = "INSERT INTO budget(category_id, price) VALUES (:category_id, :price);";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("category_id", budgetDto.getCategoryId());
        map.put("price", budgetDto.getPrice());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

            Integer budgetId = keyHolder.getKey().intValue();

            return budgetId;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            throw new RuntimeException("Failed to create budget", e);
        }
    }
}
