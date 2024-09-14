package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.CategoryCostDto;
import com.example.accountbot.dto.GetTransactionDto;
import com.example.accountbot.dto.TransactionDto;
import com.example.accountbot.repository.TransactionRepository;
import com.example.accountbot.rowmapper.CategoryCostRowMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepositoryImpl.class);
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer recordTransaction(TransactionDto transactionDto) {

        String sql = "INSERT INTO transaction(type, category_id, cost, description, date, lineuser_id) VALUES (:type, :category_id, :cost, :description, :date, :lineuser_id);";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", transactionDto.getType());

        //TODO category_id 要從 category 轉成 id
        Integer categoryId = getCategoryId(transactionDto.getCategory(), transactionDto.getLineUserId());
        map.put("category_id", categoryId);

        map.put("cost", transactionDto.getCost());
        map.put("description", transactionDto.getDescription());
        map.put("date", transactionDto.getDate());
        //TODO lineuser_id 要從 handler 拿
        map.put("lineuser_id", transactionDto.getLineUserId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

            Integer transactionId = keyHolder.getKey().intValue();

            return transactionId;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            throw new RuntimeException("Failed to record transaction", e);
        }
    }

    @Override
    public Integer getCategoryId(String categoryName, String lineUserId) {
        String sql = "SELECT id FROM category WHERE name = :name AND (lineuser_id = :lineuser_id OR lineuser_id = :share)";

        Map<String, Object> map = new HashMap<>();
        map.put("name", categoryName);
        map.put("lineuser_id", lineUserId);
        map.put("share", "share");

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);
        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            throw new RuntimeException("Failed to get categoryId", e);
        }
    }

    @Override
    public List<CategoryCostDto> getTransaction(Integer type, String category, String startDate, String endDate) {

        String sql;
        Map<String, Object> map = new HashMap<>();

        if(category.equals("all")){
            sql = "SELECT " +
                    "    c.name AS category, " +
                    "    SUM(t.cost) AS total_cost " +
                    "FROM `transaction` t " +
                    "JOIN `category` c ON t.category_id = c.id " +
                    "WHERE t.lineuser_id = :lineuser_id " +
                    "AND c.lineuser_id = t.lineuser_id " +
                    "AND t.date BETWEEN :start_date AND :end_date " +
                    "GROUP BY c.name;";
        }else {
            sql = "SELECT " +
                    "    c.name AS category, " +
                    "    SUM(t.cost) AS total_cost " +
                    "FROM `transaction` t " +
                    "JOIN `category` c ON t.category_id = c.id " +
                    "WHERE t.lineuser_id = :lineuser_id " +
                    "AND c.lineuser_id = t.lineuser_id " +
                    "AND t.category_id = :category_id " +
                    "AND t.date BETWEEN :start_date AND :end_date " +
                    "GROUP BY c.name;";

            //TODO category_id 要從 category 轉成 id
            Integer categoryId = getCategoryId(category, "Ua02e56c6d64140246d51c93a2961cf52");
            map.put("category_id", categoryId);
        }

        map.put("lineuser_id", "Ua02e56c6d64140246d51c93a2961cf52");
        map.put("start_date", startDate);
        map.put("end_date", endDate);

        log.info("startDate : " + startDate);
        log.info("endDate : " + endDate);

        try {
            return namedParameterJdbcTemplate.query(sql, map, new CategoryCostRowMapper());
        }catch (DataAccessException e){

            log.info("error : " + e.getMessage());

            throw new RuntimeException("Failed to get transaction", e);
        }
    }
}
