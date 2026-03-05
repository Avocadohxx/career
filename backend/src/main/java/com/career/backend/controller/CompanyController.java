package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Company;
import com.career.backend.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) wrapper.like(Company::getCompanyName, name);
        if (status != null) wrapper.eq(Company::getStatus, status);
        wrapper.select(Company::getId, Company::getCompanyName, Company::getIndustry,
                Company::getScale, Company::getPhone, Company::getEmail,
                Company::getStatus, Company::getCreateAt);
        wrapper.orderByDesc(Company::getCreateAt);
        return Result.success(companyService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/all")
    public Result<List<Company>> all() {
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Company::getId, Company::getCompanyName);
        return Result.success(companyService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Company> getById(@PathVariable Integer id) {
        Company c = companyService.getById(id);
        if (c != null) c.setPassword(null);
        return Result.success(c);
    }

    @PutMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Integer id, @RequestParam Integer status) {
        Company c = new Company();
        c.setId(id);
        c.setStatus(status);
        c.setUpdatedAt(LocalDateTime.now());
        companyService.updateById(c);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Company company) {
        company.setUpdatedAt(LocalDateTime.now());
        companyService.updateById(company);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        companyService.removeById(id);
        return Result.success(null);
    }

    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(companyService.count());
    }

    @GetMapping("/count-pending")
    public Result<Long> countPending() {
        return Result.success(companyService.count(
                new LambdaQueryWrapper<Company>().eq(Company::getStatus, 0)));
    }
}
