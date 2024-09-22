package com.example.accountbot.rowmapper.budget;

import com.example.accountbot.dto.budget.GetBudgetDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BudgetRowMapper implements RowMapper<GetBudgetDto> {

    @Override
    public GetBudgetDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        GetBudgetDto getBudgetDto = new GetBudgetDto();

        getBudgetDto.setId(rs.getInt("id"));
        getBudgetDto.setCategory(rs.getString("name"));
        getBudgetDto.setPrice(rs.getInt("price"));

        return getBudgetDto;
    }

}
