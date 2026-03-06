package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.backend.common.Result;
import com.career.backend.entity.*;
import com.career.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private CourseService courseService;

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

        List<Student> myStudents = studentService.list(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getCounselorId, counselorId)
                        .select(Student::getId, Student::getRealName, Student::getMajor,
                                Student::getJobIntention, Student::getDepartment));
        data.put("totalStudents", (long) myStudents.size());
        data.put("myStudents", myStudents);

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        data.put("monthPosts", guidancePostService.count(
                new LambdaQueryWrapper<GuidancePost>()
                        .eq(GuidancePost::getCounselorId, counselorId)
                        .ge(GuidancePost::getCreateAt, monthStart)));
        data.put("monthCourses", courseService.count(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCounselorId, counselorId)
                        .ge(Course::getCreateAt, monthStart)));

        List<Integer> sids = myStudents.stream().map(Student::getId).toList();

        // 批量查询所有学生的申请
        List<Application> allApps = sids.isEmpty() ? new ArrayList<>() :
                applicationService.list(new LambdaQueryWrapper<Application>()
                        .in(Application::getStudentId, sids));
        Map<Integer, List<Application>> appsByStudent = allApps.stream()
                .collect(Collectors.groupingBy(Application::getStudentId));

        // 批量查询公司名
        Set<Integer> companyIds = allApps.stream()
                .map(Application::getCompanyId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> companyNameMap = new HashMap<>();
        if (!companyIds.isEmpty()) {
            companyService.list(new LambdaQueryWrapper<Company>()
                    .in(Company::getId, companyIds)
                    .select(Company::getId, Company::getCompanyName))
                    .forEach(c -> companyNameMap.put(c.getId(), c.getCompanyName()));
        }

        // 饼图：已面试通过 / 未面试通过（有 status=3 申请即为"已面试通过"）
        long employed = myStudents.stream().filter(s ->
                appsByStudent.getOrDefault(s.getId(), List.of())
                        .stream().anyMatch(a -> Integer.valueOf(3).equals(a.getStatus()))).count();
        data.put("appStatusCounts", List.of(
                Map.of("name", "已面试通过", "value", employed),
                Map.of("name", "未面试通过", "value", myStudents.size() - employed)));

        // 总投递数
        data.put("totalApps", (long) allApps.size());

        // 条形图：近6个月
        data.put("monthlyStats", sids.isEmpty() ? buildEmptyMonthly() : buildMonthlyStats(sids));

        // 学生面邀&录用列表
        List<Map<String, Object>> studentList = new ArrayList<>();
        for (Student s : myStudents) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", s.getId());
            item.put("realName", s.getRealName());
            List<Application> sApps = appsByStudent.getOrDefault(s.getId(), List.of());
            item.put("totalApps", sApps.size());
            item.put("interviewList", sApps.stream()
                    .filter(a -> Integer.valueOf(2).equals(a.getStatus()))
                    .map(a -> companyNameMap.getOrDefault(a.getCompanyId(), "未知公司"))
                    .collect(Collectors.toList()));
            item.put("offerList", sApps.stream()
                    .filter(a -> Integer.valueOf(3).equals(a.getStatus()))
                    .map(a -> companyNameMap.getOrDefault(a.getCompanyId(), "未知公司"))
                    .collect(Collectors.toList()));
            studentList.add(item);
        }
        data.put("studentList", studentList);
        return Result.success(data);
    }

    private List<Map<String, Object>> buildEmptyMonthly() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate first = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            Map<String, Object> item = new HashMap<>();
            item.put("month", first.toString().substring(0, 7));
            item.put("applications", 0L);
            item.put("interviews", 0L);
            item.put("offers", 0L);
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> buildMonthlyStats(List<Integer> sids) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate first = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate next = first.plusMonths(1);
            LocalDateTime from = first.atStartOfDay();
            LocalDateTime to = next.atStartOfDay();
            Map<String, Object> item = new HashMap<>();
            item.put("month", first.toString().substring(0, 7));
            item.put("applications", applicationService.count(new LambdaQueryWrapper<Application>()
                    .in(Application::getStudentId, sids)
                    .ge(Application::getApplyTime, from).lt(Application::getApplyTime, to)));
            item.put("interviews", applicationService.count(new LambdaQueryWrapper<Application>()
                    .in(Application::getStudentId, sids).eq(Application::getStatus, 2)
                    .ge(Application::getUpdatedAt, from).lt(Application::getUpdatedAt, to)));
            item.put("offers", applicationService.count(new LambdaQueryWrapper<Application>()
                    .in(Application::getStudentId, sids).eq(Application::getStatus, 3)
                    .ge(Application::getUpdatedAt, from).lt(Application::getUpdatedAt, to)));
            list.add(item);
        }
        return list;
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

        List<Application> recentApps = applicationService.list(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getCompanyId, companyId)
                        .orderByDesc(Application::getApplyTime)
                        .last("LIMIT 5"));

        Set<Integer> studentIds = recentApps.stream()
                .map(Application::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> studentNames = new HashMap<>();
        if (!studentIds.isEmpty())
            studentService.list(new LambdaQueryWrapper<Student>()
                    .in(Student::getId, studentIds).select(Student::getId, Student::getRealName))
                    .forEach(s -> studentNames.put(s.getId(), s.getRealName()));

        Set<Integer> jobIds = recentApps.stream()
                .map(Application::getJobId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> jobTitles = new HashMap<>();
        if (!jobIds.isEmpty())
            jobService.list(new LambdaQueryWrapper<Job>()
                    .in(Job::getId, jobIds).select(Job::getId, Job::getTitle))
                    .forEach(j -> jobTitles.put(j.getId(), j.getTitle()));

        Company company = companyService.getById(companyId);
        String companyName = company != null ? company.getCompanyName() : "";

        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Application app : recentApps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", app.getId());
            item.put("studentId", app.getStudentId());
            item.put("studentName", studentNames.getOrDefault(app.getStudentId(), ""));
            item.put("companyName", companyName);
            item.put("jobId", app.getJobId());
            item.put("jobTitle", jobTitles.getOrDefault(app.getJobId(), ""));
            item.put("status", app.getStatus());
            item.put("applyTime", app.getApplyTime());
            item.put("interviewTime", app.getInterviewTime());
            item.put("updatedAt", app.getUpdatedAt());
            enriched.add(item);
        }
        data.put("recentApplications", enriched);
        return Result.success(data);
    }
}
