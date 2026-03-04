package com.career.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.career.backend.dto.LoginRequest;
import com.career.backend.dto.LoginResponse;
import com.career.backend.entity.Admin;
import com.career.backend.mapper.AdminMapper;
import com.career.backend.service.AdminService;
import com.career.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        Admin admin = getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUserName, request.getUsername()));
        if (admin == null) {
            throw new RuntimeException("账号不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        String token = jwtUtil.generateToken(admin.getUserName(), "admin");
        return new LoginResponse(token, "admin", admin.getRealName());
    }
}
