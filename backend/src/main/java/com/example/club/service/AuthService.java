package com.example.club.service;

import com.example.club.dto.LoginDTO;
import com.example.club.dto.PasswordDTO;
import com.example.club.dto.RegisterDTO;
import com.example.club.entity.User;
import com.example.club.vo.LoginVO;

import java.util.List;

public interface AuthService {
    LoginVO login(LoginDTO dto);
    void register(RegisterDTO dto);
    LoginVO currentUser();
    void changePassword(PasswordDTO dto);
    List<String> roles(Long userId);
}
