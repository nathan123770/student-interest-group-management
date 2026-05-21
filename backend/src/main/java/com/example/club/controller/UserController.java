package com.example.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.common.Result;
import com.example.club.entity.User;
import com.example.club.mapper.UserMapper;
import com.example.club.service.OperationLogService;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;

    @GetMapping
    public Result<List<User>> list(@RequestParam(required = false) String keyword) {
        AuthContext.requireAny("ADMIN");
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .like(keyword != null && !keyword.isBlank(), User::getUsername, keyword)
                .or(keyword != null && !keyword.isBlank(), w -> w.like(User::getRealName, keyword))
                .orderByDesc(User::getCreateTime));
        users.forEach(u -> u.setPassword(null));
        return Result.ok(users);
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        AuthContext.requireAny("ADMIN");
        User user = userMapper.selectById(id);
        user.setStatus(status);
        userMapper.updateById(user);
        operationLogService.record("USER", "STATUS", id, "更新用户状态：" + status);
        return Result.ok();
    }
}
