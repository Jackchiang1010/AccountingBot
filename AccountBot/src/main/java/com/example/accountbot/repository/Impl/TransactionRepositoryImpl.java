package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.dto.transaction.BalanceDto;
import com.example.accountbot.dto.transaction.GetAllTransactionDto;
import com.example.accountbot.dto.transaction.TransactionDto;
import com.example.accountbot.dto.transaction.UpdateTransactionDto;
import com.example.accountbot.repository.TransactionRepository;
import com.example.accountbot.rowmapper.category.CategoryCostRowMapper;
import com.example.accountbot.rowmapper.transaction.GetAllTransactionRowMapper;
import com.example.accountbot.rowmapper.transaction.UpdateTransactionRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
public class TransactionRepositoryImpl implements TransactionRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer recordTransaction(TransactionDto transactionDto) {

        String sql = "INSERT INTO transaction(type, category_id, cost, description, date, lineuser_id) VALUES (:type, :category_id, :cost, :description, :date, :lineuser_id);";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", transactionDto.getType());

        Integer categoryId = getCategoryId(transactionDto.getCategory(), transactionDto.getLineUserId());
        map.put("category_id", categoryId);

        map.put("cost", transactionDto.getCost());
        map.put("description", transactionDto.getDescription());
        map.put("date", transactionDto.getDate());
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
        String sql = "SELECT id FROM category WHERE name = :name AND lineuser_id = :lineuser_id ";

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
    public List<CategoryCostDto> getTransaction(Integer type, String category, String startDate, String endDate, String lineUserId) {

        String sql;
        Map<String, Object> map = new HashMap<>();

        if(category.equals("all")){
            sql = "SELECT " +
                    "    c.name AS category, " +
                    "    SUM(t.cost) AS total_cost " +
                    "FROM `transaction` t " +
                    "JOIN `category` c ON t.category_id = c.id " +
                    "WHERE t.lineuser_id = :lineuser_id " +
                    "AND t.type = :type " +
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
                    "AND t.type = :type " +
                    "AND c.lineuser_id = t.lineuser_id " +
                    "AND t.category_id = :category_id " +
                    "AND t.date BETWEEN :start_date AND :end_date " +
                    "GROUP BY c.name;";

            Integer categoryId = getCategoryId(category, lineUserId);
            map.put("category_id", categoryId);
        }

        map.put("type", type);
        map.put("lineuser_id", lineUserId);
        map.put("start_date", startDate);
        map.put("end_date", endDate);

        try {
            return namedParameterJdbcTemplate.query(sql, map, new CategoryCostRowMapper());
        }catch (DataAccessException e){

            log.info("error : " + e.getMessage());

            throw new RuntimeException("Failed to get transaction", e);
        }
    }

    @Override
    public UpdateTransactionDto updateTransaction(UpdateTransactionDto updatetransactionDto) {
        String sql = "UPDATE transaction SET type = :type, category_id = :category_id, cost = :cost, " +
                "description = :description, date = :date WHERE id = :id AND lineuser_id = :lineuser_id;";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", updatetransactionDto.getType());

        Integer categoryId = getCategoryId(updatetransactionDto.getCategory(), updatetransactionDto.getLineUserId());
        map.put("category_id", categoryId);

        map.put("cost", updatetransactionDto.getCost());
        map.put("description", updatetransactionDto.getDescription());
        map.put("date", updatetransactionDto.getDate());

        map.put("id", updatetransactionDto.getId());
        map.put("lineuser_id", updatetransactionDto.getLineUserId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

            String selectSql = "SELECT * FROM transaction WHERE id = :id;";
            Map<String, Object> selectMap = new HashMap<String, Object>();
            selectMap.put("id", updatetransactionDto.getId());

            UpdateTransactionDto updatedTransaction = namedParameterJdbcTemplate.queryForObject(selectSql, selectMap, new UpdateTransactionRowMapper());

            return updatedTransaction;

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM transaction WHERE id = :id;";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        Integer result = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        if(result > 0) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public BalanceDto balance(String startDate, String endDate) {

        String sql = "SELECT " +
                "SUM(CASE WHEN `type` = 1 THEN `cost` ELSE 0 END) AS total_expenses, " +
                "SUM(CASE WHEN `type` = 0 THEN `cost` ELSE 0 END) AS total_income " +
                "FROM `transaction` " +
                "WHERE `date` BETWEEN :start_date AND :end_date ;";

        Map<String, Object> map = new HashMap<>();
        map.put("start_date", startDate);
        map.put("end_date", endDate);

        return namedParameterJdbcTemplate.queryForObject(sql, map, new BeanPropertyRowMapper<>(BalanceDto.class));
    }

    @Override
    public List<GetAllTransactionDto> getAllTransaction(String startDate, String endDate, String lineUserId) {
        String sql = "SELECT t.type, c.name AS category, t.cost, t.description, t.date " +
                "FROM `transaction` t " +
                "JOIN `category` c ON t.category_id = c.id " +
                "WHERE t.lineuser_id = :lineuser_id " +
                "AND `date` BETWEEN :start_date AND :end_date ;";

        Map<String, Object> map = new HashMap<>();
        map.put("lineuser_id", lineUserId);
        map.put("start_date", startDate);
        map.put("end_date", endDate);

        try {
            return namedParameterJdbcTemplate.query(sql, map, new GetAllTransactionRowMapper());
        }catch (DataAccessException e){

            log.info("error : " + e.getMessage());

            throw new RuntimeException("Failed to get all transaction", e);
        }
    }
}
