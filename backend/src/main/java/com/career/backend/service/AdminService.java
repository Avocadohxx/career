package com.career.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.career.backend.dto.LoginRequest;
import com.career.backend.dto.LoginResponse;
import com.career.backend.entity.Admin;

public interface AdminService extends IService<Admin> {
    LoginResponse login(LoginRequest request);
}
