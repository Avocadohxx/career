package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company")
public class Company {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userName;
    private String password;
    private String companyName;
    private String industry;
    private String scale;
    private String address;
    private String phone;
    private String email;
    private String description;
    private Integer status;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
