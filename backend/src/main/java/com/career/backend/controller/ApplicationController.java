package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.*;
import com.career.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    @Autowired private ApplicationService applicationService;
    @Autowired private JobService jobService;
    @Autowired private StudentService studentService;
    @Autowired private CompanyService companyService;

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

    /** 学生投递列表（含企业名、岗位名） */
    @GetMapping("/my-list-detail")
    public Result<Map<String, Object>> myListDetail(
            @RequestParam Integer studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Application> pageResult = applicationService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getStudentId, studentId)
                        .orderByDesc(Application::getApplyTime));

        List<Application> apps = pageResult.getRecords();
        Set<Integer> jobIds = apps.stream().map(Application::getJobId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> companyIds = apps.stream().map(Application::getCompanyId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Integer, String> jobTitles = new HashMap<>();
        if (!jobIds.isEmpty())
            jobService.list(new LambdaQueryWrapper<Job>().in(Job::getId, jobIds).select(Job::getId, Job::getTitle))
                    .forEach(j -> jobTitles.put(j.getId(), j.getTitle()));

        Map<Integer, String> companyNames = new HashMap<>();
        if (!companyIds.isEmpty())
            companyService.list(new LambdaQueryWrapper<Company>().in(Company::getId, companyIds).select(Company::getId, Company::getCompanyName))
                    .forEach(c -> companyNames.put(c.getId(), c.getCompanyName()));

        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Application app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", app.getId());
            item.put("jobId", app.getJobId());
            item.put("companyId", app.getCompanyId());
            item.put("status", app.getStatus());
            item.put("interviewTime", app.getInterviewTime());
            item.put("applyTime", app.getApplyTime());
            item.put("jobTitle", jobTitles.getOrDefault(app.getJobId(), ""));
            item.put("companyName", companyNames.getOrDefault(app.getCompanyId(), ""));
            enriched.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", enriched);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    /** 企业按岗位查看投递（支持状态过滤） */
    @GetMapping("/job-list")
    public Result<?> jobList(
            @RequestParam Integer jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String statusIn) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getJobId, jobId);
        if (statusIn != null && !statusIn.isBlank()) {
            List<Integer> statuses = Arrays.stream(statusIn.split(","))
                    .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
            wrapper.in(Application::getStatus, statuses);
        }
        wrapper.orderByDesc(Application::getApplyTime);
        return Result.success(applicationService.page(new Page<>(page, size), wrapper));
    }

    /** 企业面试管理列表（status 5=待面试, 3=已录用, 6=面试不通过） */
    @GetMapping("/interview-list")
    public Result<List<Map<String, Object>>> interviewList(@RequestParam Integer companyId) {
        List<Application> apps = applicationService.list(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getCompanyId, companyId)
                        .in(Application::getStatus, List.of(5, 3, 6))
                        .orderByDesc(Application::getUpdatedAt));

        Set<Integer> jobIds = apps.stream().map(Application::getJobId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> studentIds = apps.stream().map(Application::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Integer, String> jobTitles = new HashMap<>();
        if (!jobIds.isEmpty())
            jobService.list(new LambdaQueryWrapper<Job>().in(Job::getId, jobIds).select(Job::getId, Job::getTitle))
                    .forEach(j -> jobTitles.put(j.getId(), j.getTitle()));

        Map<Integer, String> studentNames = new HashMap<>();
        if (!studentIds.isEmpty())
            studentService.list(new LambdaQueryWrapper<Student>().in(Student::getId, studentIds).select(Student::getId, Student::getRealName))
                    .forEach(s -> studentNames.put(s.getId(), s.getRealName()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Application app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", app.getId());
            item.put("jobId", app.getJobId());
            item.put("studentId", app.getStudentId());
            item.put("status", app.getStatus());
            item.put("interviewTime", app.getInterviewTime());
            item.put("updatedAt", app.getUpdatedAt());
            item.put("jobTitle", jobTitles.getOrDefault(app.getJobId(), ""));
            item.put("studentName", studentNames.getOrDefault(app.getStudentId(), ""));
            result.add(item);
        }
        return Result.success(result);
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
