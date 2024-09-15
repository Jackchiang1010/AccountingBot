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
            sql = "SELECT * FROM category WHERE type = :type AND (lineuser_id = :lineUserId OR lineuser_id = 'share');";
        }else {
            sql = "SELECT * FROM category WHERE type = :type AND (lineuser_id = :lineUserId OR lineuser_id = 'share') AND name = :name;";

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

    @Override
    public UpdateCategoryDto update(UpdateCategoryDto updateCategoryDto) {

        try {

            Integer copyCategoryId = updateCategoryDto.getId();

            if(updateCategoryDto.getId() <= 8){
                String copySql = "INSERT INTO category (type, name, lineuser_id) " +
                        "SELECT type, name, :lineuser_id FROM category " +
                        "WHERE lineuser_id = 'share' AND id = :id;";

                Map<String, Object> copyMap = new HashMap<String, Object>();
                copyMap.put("id", updateCategoryDto.getId());
                copyMap.put("lineuser_id", updateCategoryDto.getLineUserId());

                KeyHolder copyKeyHolder = new GeneratedKeyHolder();

                namedParameterJdbcTemplate.update(copySql, new MapSqlParameterSource(copyMap), copyKeyHolder);

                copyCategoryId = copyKeyHolder.getKey().intValue();
            }

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

                //TODO lineuser_id 要從 handler 拿
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
}
