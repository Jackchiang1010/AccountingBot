package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.repository.CategoryRepository;
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
public class CategoryRepositoryImpl implements CategoryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer create(CategoryDto categoryDto) {
        String sql = "INSERT INTO category(type, name, lineuser_id) VALUES (:type, :name, :lineuser_id);";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", categoryDto.getType());
        map.put("name", categoryDto.getName());
        map.put("lineuser_id", categoryDto.getLineUserId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

            Integer categoryId = keyHolder.getKey().intValue();

            return categoryId;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            throw new RuntimeException("Failed to create category", e);
        }
    }
}
