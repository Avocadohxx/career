package com.career.backend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String role;
    private String realName;
    private String userName;
    private Integer userId;
    private Integer companyStatus;

    public LoginResponse(String token, String role, String realName, String userName, Integer userId) {
        this.token = token;
        this.role = role;
        this.realName = realName;
        this.userName = userName;
        this.userId = userId;
    }

    public LoginResponse(String token, String role, String realName, String userName, Integer userId, Integer companyStatus) {
        this(token, role, realName, userName, userId);
        this.companyStatus = companyStatus;
    }
}
