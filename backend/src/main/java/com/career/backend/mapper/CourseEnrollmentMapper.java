package com.career.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.career.backend.entity.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {
}
