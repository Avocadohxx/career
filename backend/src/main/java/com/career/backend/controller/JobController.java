package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Job;
import com.career.backend.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer companyId) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isBlank()) wrapper.like(Job::getTitle, title);
        if (city != null && !city.isBlank()) wrapper.like(Job::getCity, city);
        if (status != null) wrapper.eq(Job::getStatus, status);
        if (companyId != null) wrapper.eq(Job::getCompanyId, companyId);
        wrapper.orderByDesc(Job::getCreateAt);
        return Result.success(jobService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Job> getById(@PathVariable Integer id) {
        return Result.success(jobService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Job job) {
        job.setStatus(0);
        job.setCreateAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobService.save(job);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Job job) {
        job.setUpdatedAt(LocalDateTime.now());
        jobService.updateById(job);
        return Result.success(null);
    }

    @PutMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Integer id, @RequestParam Integer status) {
        Job job = new Job();
        job.setId(id);
        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());
        jobService.updateById(job);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        jobService.removeById(id);
        return Result.success(null);
    }

    @GetMapping("/approved")
    public Result<List<Job>> approved() {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getStatus, 1);
        wrapper.select(Job::getId, Job::getTitle, Job::getSkills, Job::getDescription,
                Job::getCity, Job::getSalaryMin, Job::getSalaryMax, Job::getCompanyId);
        return Result.success(jobService.list(wrapper));
    }

    @GetMapping("/approved-by-company")
    public Result<List<Job>> approvedByCompany(@RequestParam Integer companyId) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getStatus, 1).eq(Job::getCompanyId, companyId);
        wrapper.select(Job::getId, Job::getTitle, Job::getSkills, Job::getDescription);
        return Result.success(jobService.list(wrapper));
    }

    @GetMapping("/count-today")
    public Result<Long> countToday() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        return Result.success(jobService.count(
                new LambdaQueryWrapper<Job>().ge(Job::getCreateAt, start)));
    }
}
