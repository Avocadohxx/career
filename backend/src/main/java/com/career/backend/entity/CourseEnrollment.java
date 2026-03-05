package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_enrollment")
public class CourseEnrollment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer courseId;
    private Integer studentId;
    private LocalDateTime enrollTime;
}
