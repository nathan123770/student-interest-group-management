package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String studentNo;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
}
