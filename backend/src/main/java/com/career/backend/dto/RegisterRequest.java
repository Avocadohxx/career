package com.career.backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String role;        // admin / student / counselor / company
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    // 学生特有
    private String school;
    private String major;
    private String department;
    // 辅导员特有（department 复用）
    // 企业特有
    private String companyName;
    private String industry;
}
