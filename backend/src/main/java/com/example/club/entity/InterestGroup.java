package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interest_group")
public class InterestGroup extends BaseEntity {
    private Long id;
    private String name;
    private Long categoryId;
    private Long leaderId;
    private String coverUrl;
    private String description;
    private String location;
    private Integer maxMembers;
    private Integer currentMembers;
    private Integer auditStatus;
    private Integer status;
    @TableField(exist = false)
    private String leaderName;
}
