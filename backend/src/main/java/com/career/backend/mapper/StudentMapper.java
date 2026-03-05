package com.career.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.career.backend.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
