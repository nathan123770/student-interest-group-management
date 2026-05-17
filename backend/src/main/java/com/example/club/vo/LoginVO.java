package com.example.club.vo;

import com.example.club.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginVO {
    private String token;
    private User user;
    private List<String> roles;
}
