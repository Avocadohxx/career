package com.career.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict")
public class Dict {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String dictType;
    private String dictKey;
    private String dictValue;
    private String description;
    private Integer sort;
}
