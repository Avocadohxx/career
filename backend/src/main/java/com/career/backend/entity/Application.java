package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application")
public class Application {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private Integer jobId;
    private Integer companyId;
    private Integer status;
    private LocalDateTime interviewTime;
    private LocalDateTime applyTime;
    private LocalDateTime updatedAt;
}
