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
        //TODO category_id 要從 category 轉成 id
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
    public List<BudgetDto> get(String category, String lineUserId) {
//        String sql;
//        Map<String, Object> map = new HashMap<>();
//
//        if(category.equals("all")){
//            sql = "SELECT b.id, b.category_id, b.price " +
//                    "FROM budget b " +
//                    "JOIN category c ON b.category_id = c.id " +
//                    "WHERE c.lineuser_id = :lineUserId;";
//        }else {
//            sql = "SELECT b.id, b.category_id, b.price " +
//                    "FROM budget b " +
//                    "JOIN category c ON b.category_id = c.id " +
//                    "WHERE c.lineuser_id = :lineUserId;";
//
//            map.put("lineUserId", lineUserId);
//        }
//
//        try {
//            return namedParameterJdbcTemplate.query(sql, map, new GetCategoryRowMapper());
//        }catch (DataAccessException e){
//
//            log.info("error : " + e.getMessage());
//
//            throw new RuntimeException("Failed to get category", e);
//        }
        return null;
    }
}
