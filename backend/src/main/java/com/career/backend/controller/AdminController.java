package com.career.backend.controller;

import com.career.backend.common.Result;
import com.career.backend.entity.Admin;
import com.career.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/{id}")
    public Result<Admin> getById(@PathVariable Integer id) {
        Admin a = adminService.getById(id);
        if (a != null) a.setPassword(null);
        return Result.success(a);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Admin admin) {
        admin.setUpdatedAt(LocalDateTime.now());
        adminService.updateById(admin);
        return Result.success(null);
    }
}
