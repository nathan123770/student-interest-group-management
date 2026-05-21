package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.common.PageResult;
import com.example.club.dto.OperationLogQueryDTO;
import com.example.club.entity.OperationLog;
import com.example.club.mapper.OperationLogMapper;
import com.example.club.service.OperationLogService;
import com.example.club.utils.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    @Override
    public void record(String module, String action, Long businessId, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorId(AuthContext.userId());
        log.setOperatorRoles(String.join(",", AuthContext.roles()));
        log.setModule(module);
        log.setAction(action);
        log.setBusinessId(businessId);
        log.setResult("SUCCESS");
        log.setIp(clientIp());
        log.setDetail(detail);
        save(log);
    }

    @Override
    public PageResult<OperationLog> pageLogs(OperationLogQueryDTO query) {
        AuthContext.requireAny("ADMIN");
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(query.getModule() != null && !query.getModule().isBlank(), OperationLog::getModule, query.getModule())
                .eq(query.getAction() != null && !query.getAction().isBlank(), OperationLog::getAction, query.getAction())
                .orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    private String clientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "-";
        }
        HttpServletRequest request = attrs.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
