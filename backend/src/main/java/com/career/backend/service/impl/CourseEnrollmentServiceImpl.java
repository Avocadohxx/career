package com.career.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.career.backend.entity.CourseEnrollment;
import com.career.backend.mapper.CourseEnrollmentMapper;
import com.career.backend.service.CourseEnrollmentService;
import org.springframework.stereotype.Service;

@Service
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> implements CourseEnrollmentService {
}
