package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Resume;
import com.career.backend.entity.Student;
import com.career.backend.service.ResumeService;
import com.career.backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ResumeService resumeService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) Integer counselorId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) wrapper.like(Student::getRealName, name);
        if (department != null && !department.isBlank()) wrapper.like(Student::getDepartment, department);
        if (major != null && !major.isBlank()) wrapper.like(Student::getMajor, major);
        if (counselorId != null) wrapper.eq(Student::getCounselorId, counselorId);
        wrapper.select(Student::getId, Student::getUserName, Student::getRealName, Student::getSchool,
                Student::getDepartment, Student::getMajor, Student::getJobIntention,
                Student::getCounselorId, Student::getPhone, Student::getEmail, Student::getCreateAt);
        wrapper.orderByDesc(Student::getCreateAt);
        return Result.success(studentService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/all")
    public Result<List<Student>> all() {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Student::getId, Student::getRealName, Student::getUserName,
                Student::getSchool, Student::getDepartment, Student::getMajor,
                Student::getJobIntention, Student::getCityIntention,
                Student::getPhone, Student::getEmail, Student::getCounselorId);
        return Result.success(studentService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Integer id) {
        Student s = studentService.getById(id);
        if (s != null) s.setPassword(null);
        return Result.success(s);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Student student) {
        if (student.getPassword() != null && !student.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(student.getPassword()));
        }
        student.setCreateAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        studentService.save(student);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Student student) {
        student.setUpdatedAt(LocalDateTime.now());
        studentService.updateById(student);
        // 同步求职意向、意向城市到 resume 表
        Resume resume = resumeService.getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getStudentId, student.getId()));
        if (resume != null) {
            resume.setJobIntention(student.getJobIntention());
            resume.setCityIntention(student.getCityIntention());
            resume.setUpdatedAt(LocalDateTime.now());
            resumeService.updateById(resume);
        }
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        studentService.removeById(id);
        return Result.success(null);
    }

    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(studentService.count());
    }
}
