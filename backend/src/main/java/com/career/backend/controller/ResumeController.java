package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.backend.common.Result;
import com.career.backend.entity.Resume;
import com.career.backend.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

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
        return Result.success(null);
    }
}
