package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.dto.LoginDTO;
import com.example.club.dto.PasswordDTO;
import com.example.club.dto.RegisterDTO;
import com.example.club.entity.Role;
import com.example.club.entity.User;
import com.example.club.entity.UserRole;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.RoleMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.mapper.UserRoleMapper;
import com.example.club.service.AuthService;
import com.example.club.utils.AuthContext;
import com.example.club.utils.JwtUtils;
import com.example.club.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }
        List<String> roles = roles(user.getId());
        user.setPassword(null);
        return new LoginVO(jwtUtils.generateToken(user.getId(), user.getUsername(), roles), user, roles);
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setStudentNo(dto.getStudentNo());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        userMapper.insert(user);
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "STUDENT"));
        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public LoginVO currentUser() {
        User user = userMapper.selectById(AuthContext.userId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        List<String> roles = roles(user.getId());
        user.setPassword(null);
        return new LoginVO(jwtUtils.generateToken(user.getId(), user.getUsername(), roles), user, roles);
    }

    @Override
    public void changePassword(PasswordDTO dto) {
        User user = userMapper.selectById(AuthContext.userId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public List<String> roles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream().map(Role::getCode).toList();
    }
}
