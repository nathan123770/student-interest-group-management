package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_category")
public class GroupCategory extends BaseEntity {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Integer sortOrder;
}
