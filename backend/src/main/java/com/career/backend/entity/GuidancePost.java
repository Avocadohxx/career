package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("guidance_post")
public class GuidancePost {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer counselorId;
    private String title;
    private String content;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
