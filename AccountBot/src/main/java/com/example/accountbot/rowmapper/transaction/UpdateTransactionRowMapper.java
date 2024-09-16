package com.example.accountbot.rowmapper.transaction;

import com.example.accountbot.dto.transaction.UpdateTransactionDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateTransactionRowMapper implements RowMapper<UpdateTransactionDto> {

    @Override
    public UpdateTransactionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UpdateTransactionDto updateTransactionDto = new UpdateTransactionDto();

        updateTransactionDto.setId(rs.getInt("id"));
        updateTransactionDto.setType(rs.getInt("type"));
        updateTransactionDto.setCategory(rs.getString("category_id"));
        updateTransactionDto.setCost(rs.getInt("cost"));
        updateTransactionDto.setDescription(rs.getString("description"));
        updateTransactionDto.setDate(rs.getString("date"));
        updateTransactionDto.setLineUserId(rs.getString("lineuser_id"));

        return updateTransactionDto;
    }

}
