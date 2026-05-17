package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {
    private Long id;
    private String title;
    private String content;
    private String noticeType;
    private Long groupId;
    private Long publisherId;
    private Integer topFlag;
    private Integer status;
}
