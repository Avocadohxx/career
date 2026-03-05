package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job")
public class Job {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer companyId;
    private String title;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String education;
    private String jobType;
    private String skills;
    private String description;
    private Integer status;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
