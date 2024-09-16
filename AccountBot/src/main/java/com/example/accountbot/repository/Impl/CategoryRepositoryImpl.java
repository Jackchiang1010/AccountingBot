package com.example.accountbot.repository.Impl;

import com.example.accountbot.dto.category.CategoryDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.repository.CategoryRepository;
import com.example.accountbot.rowmapper.GetCategoryRowMapper;
import com.example.accountbot.rowmapper.UpdateCategoryRowMapper;
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

    @Override
    public List<CategoryDto> get(Integer type, String name, String lineUserId) {
        String sql;
        Map<String, Object> map = new HashMap<>();

        if(name.equals("all")){
            sql = "SELECT * FROM category WHERE type = :type AND lineuser_id = :lineUserId;";
        }else {
            sql = "SELECT * FROM category WHERE type = :type AND lineuser_id = :lineUserId AND name = :name;";

            map.put("name", name);
        }

        map.put("type", type);
        map.put("lineUserId", lineUserId);

        try {
            return namedParameterJdbcTemplate.query(sql, map, new GetCategoryRowMapper());
        }catch (DataAccessException e){

            log.info("error : " + e.getMessage());

            throw new RuntimeException("Failed to get category", e);
        }
    }


    // 檢查並插入預設分類
    @Override
    public void initializeDefaultCategories(String lineUserId) {
        String checkSql = "SELECT COUNT(*) FROM category WHERE lineuser_id = :lineUserId";
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("lineUserId", lineUserId);

        Integer count = namedParameterJdbcTemplate.queryForObject(checkSql, checkMap, Integer.class);

        if (count != null && count == 0) {
            String insertSql = "INSERT INTO category (type, name, lineuser_id) VALUES " +
                    "(1, '飲食', :lineUserId), " +
                    "(1, '娛樂', :lineUserId), " +
                    "(1, '交通', :lineUserId), " +
                    "(1, '藥妝', :lineUserId), " +
                    "(0, '薪水', :lineUserId), " +
                    "(0, '獎金', :lineUserId), " +
                    "(0, '兼職', :lineUserId), " +
                    "(0, '投資', :lineUserId)";

            namedParameterJdbcTemplate.update(insertSql, checkMap);
        }
    }

    @Override
    public UpdateCategoryDto update(UpdateCategoryDto updateCategoryDto) {

        try {

            Integer copyCategoryId = updateCategoryDto.getId();

            String checkSql = "SELECT COUNT(*) FROM category WHERE lineuser_id = :lineuser_id AND name = :name";
            Map<String, Object> params = new HashMap<>();
            params.put("lineuser_id", updateCategoryDto.getLineUserId());
            params.put("name", updateCategoryDto.getName());

            int count = namedParameterJdbcTemplate.queryForObject(checkSql, params, Integer.class);

            if (count > 0) {
                // 分類名稱已存在
                throw new IllegalArgumentException("分類名稱已存在，請選擇不同的名稱。");
            } else {
                // 分類名稱不存在
                String updateSql = "UPDATE category SET type = :type, name = :name " +
                        "WHERE lineuser_id = :lineuser_id AND id = :id;";


                Map<String, Object> map = new HashMap<String, Object>();
                map.put("type", updateCategoryDto.getType());
                map.put("name", updateCategoryDto.getName());
                map.put("id", copyCategoryId);
                map.put("lineuser_id", updateCategoryDto.getLineUserId());

                KeyHolder keyHolder = new GeneratedKeyHolder();

                namedParameterJdbcTemplate.update(updateSql, new MapSqlParameterSource(map), keyHolder);

                String selectSql = "SELECT * FROM category WHERE id = :id;";
                Map<String, Object> selectMap = new HashMap<String, Object>();
                selectMap.put("id", copyCategoryId);

                UpdateCategoryDto updatedCategory = namedParameterJdbcTemplate.queryForObject(selectSql, selectMap, new UpdateCategoryRowMapper());

                return updatedCategory;
            }

        }catch (DataAccessException e){
            log.info("error : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM category WHERE id = :id;";

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
}
