package com.example.accountbot.rowmapper.transaction;

import com.example.accountbot.dto.transaction.GetAllTransactionDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetAllTransactionRowMapper implements RowMapper<GetAllTransactionDto> {

    @Override
    public GetAllTransactionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        GetAllTransactionDto getAllTransactionDto = new GetAllTransactionDto();

        getAllTransactionDto.setId(rs.getInt("id"));
        getAllTransactionDto.setType(rs.getInt("type"));
        getAllTransactionDto.setCategory(rs.getString("category"));
        getAllTransactionDto.setCost(rs.getInt("cost"));
        getAllTransactionDto.setDescription(rs.getString("description"));
        getAllTransactionDto.setDate(rs.getString("date"));

        return getAllTransactionDto;
    }

}
