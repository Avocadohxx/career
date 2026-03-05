package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Student;
import com.career.backend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String major) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) wrapper.like(Student::getRealName, name);
        if (department != null && !department.isBlank()) wrapper.like(Student::getDepartment, department);
        if (major != null && !major.isBlank()) wrapper.like(Student::getMajor, major);
        wrapper.select(Student::getId, Student::getUserName, Student::getRealName, Student::getSchool,
                Student::getDepartment, Student::getMajor, Student::getJobIntention,
                Student::getCounselorId, Student::getPhone, Student::getEmail, Student::getCreateAt);
        wrapper.orderByDesc(Student::getCreateAt);
        return Result.success(studentService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/all")
    public Result<List<Student>> all() {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Student::getId, Student::getRealName, Student::getUserName, Student::getDepartment, Student::getMajor);
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
        student.setCreateAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        studentService.save(student);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Student student) {
        student.setUpdatedAt(LocalDateTime.now());
        studentService.updateById(student);
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
