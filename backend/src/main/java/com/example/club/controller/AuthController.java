package com.example.club.controller;

import com.example.club.common.Result;
import com.example.club.dto.LoginDTO;
import com.example.club.dto.PasswordDTO;
import com.example.club.dto.RegisterDTO;
import com.example.club.service.AuthService;
import com.example.club.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<LoginVO> me() {
        return Result.ok(authService.currentUser());
    }

    @PostMapping("/password")
    public Result<Void> password(@RequestBody PasswordDTO dto) {
        authService.changePassword(dto);
        return Result.ok();
    }
}
