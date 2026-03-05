package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private String avatar;
    private String jobIntention;
    private String cityIntention;
    private String education;
    private String skills;
    private String selfEval;
    private String eduHistory;
    private String workHistory;
    private LocalDateTime updatedAt;
}
