package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.common.PageResult;
import com.example.club.dto.OperationLogQueryDTO;
import com.example.club.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    void record(String module, String action, Long businessId, String detail);
    PageResult<OperationLog> pageLogs(OperationLogQueryDTO query);
}
