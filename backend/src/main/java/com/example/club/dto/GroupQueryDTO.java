package com.example.club.dto;

import lombok.Data;

@Data
public class GroupQueryDTO {
    private Long page = 1L;
    private Long size = 10L;
    private String name;
    private Long categoryId;
    private Integer auditStatus;
    private Integer status;
}
