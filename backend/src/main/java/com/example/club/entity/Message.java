package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {
    private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private String businessType;
    private Long businessId;
    private Integer readStatus;
}
