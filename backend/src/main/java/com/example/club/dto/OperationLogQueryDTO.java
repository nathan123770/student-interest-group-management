package com.example.club.dto;

import lombok.Data;

@Data
public class OperationLogQueryDTO {
    private Long page = 1L;
    private Long size = 10L;
    private String module;
    private String action;
}
