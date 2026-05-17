package com.example.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.common.Result;
import com.example.club.entity.Role;
import com.example.club.entity.UserRole;
import com.example.club.mapper.RoleMapper;
import com.example.club.mapper.UserRoleMapper;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @GetMapping
    public Result<List<Role>> list() {
        AuthContext.requireAny("ADMIN");
        return Result.ok(roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getId)));
    }

    @PostMapping("/assign")
    public Result<Void> assign(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        AuthContext.requireAny("ADMIN");
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        return Result.ok();
    }
}
