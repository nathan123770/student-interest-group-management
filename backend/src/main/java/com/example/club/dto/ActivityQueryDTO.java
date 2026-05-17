package com.example.club.dto;

import lombok.Data;

@Data
public class ActivityQueryDTO {
    private Long page = 1L;
    private Long size = 10L;
    private Long groupId;
    private String title;
    private Integer status;
}
