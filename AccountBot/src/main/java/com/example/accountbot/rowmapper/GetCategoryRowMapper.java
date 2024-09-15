package com.example.accountbot.rowmapper;

import com.example.accountbot.dto.category.CategoryCostDto;
import com.example.accountbot.dto.category.CategoryDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetCategoryRowMapper implements RowMapper<CategoryDto> {

    @Override
    public CategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setType(rs.getInt("type"));
        categoryDto.setName(rs.getString("name"));
        categoryDto.setLineUserId(rs.getString("lineuser_id"));

        return categoryDto;
    }

}
