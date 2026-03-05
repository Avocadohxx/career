package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Counselor;
import com.career.backend.service.CounselorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/counselor")
public class CounselorController {

    @Autowired
    private CounselorService counselorService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department) {
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) wrapper.like(Counselor::getRealName, name);
        if (department != null && !department.isBlank()) wrapper.like(Counselor::getDepartment, department);
        wrapper.select(Counselor::getId, Counselor::getUserName, Counselor::getRealName,
                Counselor::getDepartment, Counselor::getPhone, Counselor::getEmail, Counselor::getCreateAt);
        wrapper.orderByDesc(Counselor::getCreateAt);
        return Result.success(counselorService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/all")
    public Result<List<Counselor>> all() {
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Counselor::getId, Counselor::getRealName, Counselor::getDepartment);
        return Result.success(counselorService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Counselor> getById(@PathVariable Integer id) {
        Counselor c = counselorService.getById(id);
        if (c != null) c.setPassword(null);
        return Result.success(c);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Counselor counselor) {
        counselor.setCreateAt(LocalDateTime.now());
        counselor.setUpdatedAt(LocalDateTime.now());
        counselorService.save(counselor);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Counselor counselor) {
        counselor.setUpdatedAt(LocalDateTime.now());
        counselorService.updateById(counselor);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        counselorService.removeById(id);
        return Result.success(null);
    }
}
