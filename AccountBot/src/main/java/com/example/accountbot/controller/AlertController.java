package com.example.accountbot.controller;

import com.example.accountbot.dto.ErrorResponseDto;
import com.example.accountbot.dto.alert.AlertDto;
import com.example.accountbot.dto.alert.UpdateAlertDto;
import com.example.accountbot.dto.category.UpdateCategoryDto;
import com.example.accountbot.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/1.0/alert")
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody AlertDto alertDto) {

        try {
            Map<String, Object> response = alertService.create(alertDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get")
    public ResponseEntity<?> get(@RequestParam(value = "lineUserId") String lineUserId) {

        try {
            Map<String, Object> response = alertService.get(lineUserId);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateAlertDto updateAlertDto) {

        try {
            Map<String, Object> response = alertService.update(updateAlertDto);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody UpdateCategoryDto updateCategoryDto) {

        try {
            boolean result = alertService.delete(updateCategoryDto.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("result", result);

            if(result == true) {
                return ResponseEntity.ok(response);
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }catch (RuntimeException e){
            return new ResponseEntity<>(ErrorResponseDto.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            return new ResponseEntity<>(ErrorResponseDto.error("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
