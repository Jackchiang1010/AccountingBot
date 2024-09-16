package com.example.accountbot.rowmapper.category;

import com.example.accountbot.dto.category.CategoryCostDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryCostRowMapper implements RowMapper<CategoryCostDto> {

    @Override
    public CategoryCostDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        CategoryCostDto categoryCostDto = new CategoryCostDto();

        categoryCostDto.setCategory(rs.getString("category"));
        categoryCostDto.setTotalCost(rs.getInt("total_cost"));

        return categoryCostDto;
    }

}
