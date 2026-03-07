package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.backend.common.Result;
import com.career.backend.entity.Resume;
import com.career.backend.entity.Student;
import com.career.backend.service.ResumeService;
import com.career.backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private StudentService studentService;

    @GetMapping("/my")
    public Result<Resume> my(@RequestParam Integer studentId) {
        Resume resume = resumeService.getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, studentId));
        return Result.success(resume);
    }

    @GetMapping("/by-student/{studentId}")
    public Result<Resume> byStudent(@PathVariable Integer studentId) {
        Resume resume = resumeService.getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, studentId));
        return Result.success(resume);
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> allResumes() {
        List<Resume> resumes = resumeService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Resume r : resumes) {
            Student stu = studentService.getById(r.getStudentId());
            if (stu == null) continue;
            Map<String, Object> item = new HashMap<>();
            String name = stu.getRealName();
            item.put("studentName", name != null && name.length() >= 2 ? name.charAt(0) + "**" : name);
            item.put("skills", r.getSkills());
            item.put("selfEval", r.getSelfEval());
            item.put("studentId", r.getStudentId());
            result.add(item);
        }
        return Result.success(result);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody Resume resume) {
        resume.setUpdatedAt(LocalDateTime.now());
        Resume existing = resumeService.getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, resume.getStudentId()));
        if (existing != null) {
            resume.setId(existing.getId());
            resumeService.updateById(resume);
        } else {
            resumeService.save(resume);
        }
        // 同步求职意向、意向城市到 student 表
        if (resume.getStudentId() != null) {
            Student stu = new Student();
            stu.setId(resume.getStudentId());
            stu.setJobIntention(resume.getJobIntention());
            stu.setCityIntention(resume.getCityIntention());
            stu.setUpdatedAt(LocalDateTime.now());
            studentService.updateById(stu);
        }
        return Result.success(null);
    }
}
