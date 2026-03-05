package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.Course;
import com.career.backend.entity.CourseEnrollment;
import com.career.backend.service.CourseEnrollmentService;
import com.career.backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseEnrollmentService enrollmentService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer counselorId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (counselorId != null) wrapper.eq(Course::getCounselorId, counselorId);
        wrapper.orderByDesc(Course::getCreateAt);
        return Result.success(courseService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Course course) {
        course.setEnrolled(0);
        course.setCreateAt(LocalDateTime.now());
        courseService.save(course);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Course course) {
        courseService.updateById(course);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        courseService.removeById(id);
        return Result.success(null);
    }

    @PostMapping("/enroll/{courseId}")
    public Result<Void> enroll(@PathVariable Integer courseId, @RequestParam Integer studentId) {
        Course course = courseService.getById(courseId);
        if (course == null) throw new RuntimeException("课程不存在");
        if (course.getEnrolled() >= course.getMaxStudents()) throw new RuntimeException("报名人数已满");
        boolean exists = enrollmentService.count(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStudentId, studentId)) > 0;
        if (exists) throw new RuntimeException("您已报名该课程");
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setEnrollTime(LocalDateTime.now());
        enrollmentService.save(enrollment);
        course.setEnrolled(course.getEnrolled() + 1);
        courseService.updateById(course);
        return Result.success(null);
    }

    @DeleteMapping("/unenroll/{courseId}")
    public Result<Void> unenroll(@PathVariable Integer courseId, @RequestParam Integer studentId) {
        enrollmentService.remove(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStudentId, studentId));
        Course course = courseService.getById(courseId);
        if (course != null && course.getEnrolled() > 0) {
            course.setEnrolled(course.getEnrolled() - 1);
            courseService.updateById(course);
        }
        return Result.success(null);
    }

    @GetMapping("/enrolled-students/{courseId}")
    public Result<List<CourseEnrollment>> enrolledStudents(@PathVariable Integer courseId) {
        return Result.success(enrollmentService.list(
                new LambdaQueryWrapper<CourseEnrollment>().eq(CourseEnrollment::getCourseId, courseId)));
    }

    @GetMapping("/my-enrollments")
    public Result<List<CourseEnrollment>> myEnrollments(@RequestParam Integer studentId) {
        return Result.success(enrollmentService.list(
                new LambdaQueryWrapper<CourseEnrollment>().eq(CourseEnrollment::getStudentId, studentId)));
    }
}
