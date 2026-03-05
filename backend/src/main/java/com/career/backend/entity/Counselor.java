package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("counselor")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userName;
    private String password;
    private String realName;
    private String department;
    private String phone;
    private String email;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
