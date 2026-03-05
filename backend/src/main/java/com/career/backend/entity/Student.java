package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userName;
    private String password;
    private String realName;
    private String school;
    private String major;
    private String department;
    private String jobIntention;
    private String cityIntention;
    private String phone;
    private String email;
    private String address;
    private Integer counselorId;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
