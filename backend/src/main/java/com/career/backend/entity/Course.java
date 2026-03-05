package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer counselorId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private Integer maxStudents;
    private Integer enrolled;
    private Integer status;
    private LocalDateTime createAt;
}
