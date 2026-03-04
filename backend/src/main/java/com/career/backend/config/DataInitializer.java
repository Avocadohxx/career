package com.career.backend.config;

import com.career.backend.entity.Admin;
import com.career.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时检查是否存在默认管理员，若无则自动创建
 * 默认账号：admin  密码：admin123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminService adminService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminService.count() == 0) {
            Admin admin = new Admin();
            admin.setUserName("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@career.com");
            admin.setCreateAt(LocalDateTime.now());
            adminService.save(admin);
            System.out.println("✅ 默认管理员已创建：账号 admin / 密码 admin123");
        }
    }
}
