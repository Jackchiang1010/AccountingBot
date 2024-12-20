package com.example.accountbot.rowmapper.alert;

import com.example.accountbot.dto.alert.GetAlertDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetAlertRowMapper implements RowMapper<GetAlertDto> {

    @Override
    public GetAlertDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        GetAlertDto getAlertDto = new GetAlertDto();

        getAlertDto.setId(rs.getInt("id"));
        getAlertDto.setTime(rs.getString("time"));
        getAlertDto.setDescription(rs.getString("description"));

        return getAlertDto;
    }

}
