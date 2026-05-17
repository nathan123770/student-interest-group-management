package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("join_apply")
public class JoinApply extends BaseEntity {
    private Long id;
    private Long groupId;
    private Long userId;
    private String reason;
    private Integer status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String groupName;
    @TableField(exist = false)
    private String reviewerName;
}
