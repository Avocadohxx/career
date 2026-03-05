package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private String title;
    private String content;
    private Integer replyCount;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
