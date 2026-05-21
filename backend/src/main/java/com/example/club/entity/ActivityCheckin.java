package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_checkin")
public class ActivityCheckin extends BaseEntity {
    private Long id;
    private Long activityId;
    private Long userId;
    private Integer status;
    private LocalDateTime checkinTime;
    private String checkinMethod;
    private Long operatorId;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String activityTitle;
    @TableField(exist = false)
    private LocalDateTime activityStartTime;
}
