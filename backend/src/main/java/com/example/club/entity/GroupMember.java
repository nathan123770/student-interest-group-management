package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_member")
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private String memberRole;
    private LocalDateTime joinTime;
    private Integer status;
    @TableField(exist = false)
    private String userName;
}
