package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.budget.BudgetDto;
import com.example.accountbot.dto.budget.GetBudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetDto;
import com.example.accountbot.dto.budget.UpdateBudgetResponseDto;
import com.example.accountbot.repository.BudgetRepository;
import com.example.accountbot.rowmapper.budget.BudgetRowMapper;
import com.example.accountbot.rowmapper.budget.UpdateBudgetRowMapper;
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
public class BudgetRepositoryImpl implements BudgetRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer create(BudgetDto budgetDto) {
        String sql = "INSERT INTO budget(category_id, price) VALUES (:category_id, :price);";

        Map<String, Object> map = new HashMap<String, Object>();

        Integer categoryId = getCategoryId(budgetDto.getCategory(), budgetDto.getLineUserId());
        map.put("category_id", categoryId);
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

    @Override
    public Integer getCategoryId(String categoryName, String lineUserId) {
        String sql = "SELECT id FROM category WHERE name = :name AND lineuser_id = :lineuser_id;";

        Map<String, Object> map = new HashMap<>();
        map.put("name", categoryName);
        map.put("lineuser_id", lineUserId);

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);
        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            throw new RuntimeException("Failed to get categoryId", e);
        }
    }

    @Override
    public List<GetBudgetDto> get(String category, String lineUserId) {
        String sql;
        Map<String, Object> map = new HashMap<>();

        if(category.equals("all")){
            sql = "SELECT c.name, b.price " +
                    "FROM budget b " +
                    "JOIN category c ON b.category_id = c.id " +
                    "WHERE c.type = 1 " +
                    "AND c.lineuser_id = :lineUserId;";
        }else {
            sql = "SELECT c.name, b.price " +
                    "FROM budget b " +
                    "JOIN category c ON b.category_id = c.id " +
                    "WHERE c.type = 1 " +
                    "AND c.lineuser_id = :lineUserId " +
                    "AND b.category_id = :categoryId;";

            Integer categoryId = getCategoryId(category, lineUserId);
            map.put("categoryId", categoryId);
        }

        map.put("lineUserId", lineUserId);

        try {
            return namedParameterJdbcTemplate.query(sql, map, new BudgetRowMapper());
        }catch (DataAccessException e){

            log.info("error : " + e.getMessage());

            throw new RuntimeException("Failed to get budget", e);
        }
    }

    @Override
    public UpdateBudgetResponseDto update(UpdateBudgetDto updateBudgetDto) {
        try {

            String updateSql = "UPDATE budget SET price = :price, category_id = :categoryId WHERE id = :id;";

            Map<String, Object> map = new HashMap<String, Object>();
            Integer categoryId = getCategoryId(updateBudgetDto.getCategory(), updateBudgetDto.getLineUserId());
            map.put("price", updateBudgetDto.getPrice());
            map.put("categoryId", categoryId);
            map.put("id", updateBudgetDto.getId());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            namedParameterJdbcTemplate.update(updateSql, new MapSqlParameterSource(map), keyHolder);

            String selectSql = "SELECT b.id, c.name, b.price " +
                    "FROM budget b " +
                    "JOIN category c ON b.category_id = c.id " +
                    "WHERE b.id = :id;";
            Map<String, Object> selectMap = new HashMap<String, Object>();
            selectMap.put("id", updateBudgetDto.getId());

            UpdateBudgetResponseDto updatedBudgetResponse = namedParameterJdbcTemplate.queryForObject(selectSql, selectMap, new UpdateBudgetRowMapper());

            return updatedBudgetResponse;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update budget", e);
        }
    }
}
