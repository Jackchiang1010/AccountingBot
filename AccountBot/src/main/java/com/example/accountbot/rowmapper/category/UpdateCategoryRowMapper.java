package com.example.accountbot.rowmapper.category;

import com.example.accountbot.dto.category.UpdateCategoryDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateCategoryRowMapper implements RowMapper<UpdateCategoryDto> {

    @Override
    public UpdateCategoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UpdateCategoryDto updatecategoryDto = new UpdateCategoryDto();

        updatecategoryDto.setId(rs.getInt("id"));
        updatecategoryDto.setType(rs.getInt("type"));
        updatecategoryDto.setName(rs.getString("name"));
        updatecategoryDto.setLineUserId(rs.getString("lineuser_id"));

        return updatecategoryDto;
    }

}
