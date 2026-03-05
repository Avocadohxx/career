package com.career.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.career.backend.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
