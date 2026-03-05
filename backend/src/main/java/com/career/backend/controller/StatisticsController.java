package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.backend.common.Result;
import com.career.backend.entity.*;
import com.career.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private JobService jobService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private GuidancePostService guidancePostService;

    @GetMapping("/admin/overview")
    public Result<Map<String, Object>> adminOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", studentService.count());
        data.put("totalCompanies", companyService.count());
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        data.put("todayJobs", jobService.count(
                new LambdaQueryWrapper<Job>().ge(Job::getCreateAt, todayStart)));
        data.put("pendingCompanies", companyService.count(
                new LambdaQueryWrapper<Company>().eq(Company::getStatus, 0)));

        // 近7天新增学生（按天）
        List<Map<String, Object>> dailyStudents = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = studentService.count(new LambdaQueryWrapper<Student>()
                    .ge(Student::getCreateAt, date.atStartOfDay())
                    .lt(Student::getCreateAt, date.plusDays(1).atStartOfDay()));
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            dailyStudents.add(item);
        }
        data.put("dailyStudents", dailyStudents);

        // 最近注册企业（状态0）
        data.put("recentCompanies", companyService.list(
                new LambdaQueryWrapper<Company>()
                        .select(Company::getId, Company::getCompanyName, Company::getIndustry,
                                Company::getStatus, Company::getCreateAt)
                        .orderByDesc(Company::getCreateAt)
                        .last("LIMIT 5")));
        return Result.success(data);
    }

    @GetMapping("/counselor/overview")
    public Result<Map<String, Object>> counselorOverview(@RequestParam Integer counselorId) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", studentService.count(
                new LambdaQueryWrapper<Student>().eq(Student::getCounselorId, counselorId)));

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        data.put("monthPosts", guidancePostService.count(
                new LambdaQueryWrapper<GuidancePost>()
                        .eq(GuidancePost::getCounselorId, counselorId)
                        .ge(GuidancePost::getCreateAt, monthStart)));

        data.put("myStudents", studentService.list(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getCounselorId, counselorId)
                        .select(Student::getId, Student::getRealName, Student::getMajor,
                                Student::getJobIntention, Student::getDepartment)
                        .last("LIMIT 10")));
        return Result.success(data);
    }

    @GetMapping("/company/overview")
    public Result<Map<String, Object>> companyOverview(@RequestParam Integer companyId) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalJobs", jobService.count(
                new LambdaQueryWrapper<Job>().eq(Job::getCompanyId, companyId)));
        data.put("totalApplications", applicationService.count(
                new LambdaQueryWrapper<Application>().eq(Application::getCompanyId, companyId)));
        data.put("pendingApplications", applicationService.count(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getCompanyId, companyId)
                        .eq(Application::getStatus, 2)));

        data.put("recentApplications", applicationService.list(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getCompanyId, companyId)
                        .orderByDesc(Application::getApplyTime)
                        .last("LIMIT 5")));
        return Result.success(data);
    }
}
