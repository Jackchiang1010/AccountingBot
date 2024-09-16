package com.example.accountbot.rowmapper.budget;

import com.example.accountbot.dto.budget.UpdateBudgetResponseDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateBudgetRowMapper implements RowMapper<UpdateBudgetResponseDto> {

    @Override
    public UpdateBudgetResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UpdateBudgetResponseDto updateBudgetResponseDto = new UpdateBudgetResponseDto();

        updateBudgetResponseDto.setId(rs.getInt("id"));
        updateBudgetResponseDto.setCategory(rs.getString("name"));
        updateBudgetResponseDto.setPrice(rs.getInt("price"));

        return updateBudgetResponseDto;
    }

}
