package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_signup")
public class ActivitySignup {
    private Long id;
    private Long activityId;
    private Long userId;
    private LocalDateTime signupTime;
    private Integer status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String activityTitle;
    @TableField(exist = false)
    private String reviewerName;
}
