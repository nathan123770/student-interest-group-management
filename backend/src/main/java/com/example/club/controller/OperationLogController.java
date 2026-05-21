package com.example.club.controller;

import com.example.club.common.PageResult;
import com.example.club.common.Result;
import com.example.club.dto.OperationLogQueryDTO;
import com.example.club.entity.OperationLog;
import com.example.club.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {
    private final OperationLogService operationLogService;

    @GetMapping("/page")
    public Result<PageResult<OperationLog>> page(OperationLogQueryDTO query) {
        return Result.ok(operationLogService.pageLogs(query));
    }
}
