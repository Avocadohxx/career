package com.career.backend.service;

import com.career.backend.dto.LoginRequest;
import com.career.backend.dto.LoginResponse;
import com.career.backend.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    void resetPassword(RegisterRequest request);
}
