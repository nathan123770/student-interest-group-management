package com.example.club.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {
    private Long id;
    private Long operatorId;
    private String operatorRoles;
    private String module;
    private String action;
    private Long businessId;
    private String result;
    private String ip;
    private String detail;
}
