package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Application;
import com.career.backend.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody Application application) {
        boolean exists = applicationService.count(new LambdaQueryWrapper<Application>()
                .eq(Application::getStudentId, application.getStudentId())
                .eq(Application::getJobId, application.getJobId())) > 0;
        if (exists) throw new RuntimeException("您已投递过该岗位");
        application.setStatus(0);
        application.setApplyTime(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        applicationService.save(application);
        return Result.success(null);
    }

    @GetMapping("/my-list")
    public Result<?> myList(
            @RequestParam Integer studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getStudentId, studentId)
                .orderByDesc(Application::getApplyTime);
        return Result.success(applicationService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/job-list")
    public Result<?> jobList(
            @RequestParam Integer jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getJobId, jobId)
                .orderByDesc(Application::getApplyTime);
        return Result.success(applicationService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/company-list")
    public Result<?> companyList(
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getCompanyId, companyId)
                .orderByDesc(Application::getApplyTime);
        return Result.success(applicationService.page(new Page<>(page, size), wrapper));
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Integer id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String interviewTime) {
        Application app = new Application();
        app.setId(id);
        app.setStatus(status);
        app.setUpdatedAt(LocalDateTime.now());
        if (interviewTime != null && !interviewTime.isBlank()) {
            app.setInterviewTime(LocalDateTime.parse(interviewTime));
        }
        applicationService.updateById(app);
        return Result.success(null);
    }

    @GetMapping("/count-company")
    public Result<Long> countByCompany(@RequestParam Integer companyId) {
        return Result.success(applicationService.count(
                new LambdaQueryWrapper<Application>().eq(Application::getCompanyId, companyId)));
    }
}
