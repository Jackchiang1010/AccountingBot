package com.example.accountbot.rowmapper.alert;

import com.example.accountbot.dto.alert.UpdateAlertDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateAlertRowMapper implements RowMapper<UpdateAlertDto> {

    @Override
    public UpdateAlertDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UpdateAlertDto updateAlertDto = new UpdateAlertDto();

        updateAlertDto.setId(rs.getInt("id"));
        updateAlertDto.setTime(rs.getString("time"));
        updateAlertDto.setDescription(rs.getString("description"));
        updateAlertDto.setLineUserId(rs.getString("lineuser_id"));

        return updateAlertDto;
    }

}
