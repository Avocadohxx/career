package com.career.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.career.backend.entity.GuidancePost;
import com.career.backend.mapper.GuidancePostMapper;
import com.career.backend.service.GuidancePostService;
import org.springframework.stereotype.Service;

@Service
public class GuidancePostServiceImpl extends ServiceImpl<GuidancePostMapper, GuidancePost> implements GuidancePostService {
}
