package com.career.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.career.backend.entity.ForumReply;
import com.career.backend.mapper.ForumReplyMapper;
import com.career.backend.service.ForumReplyService;
import org.springframework.stereotype.Service;

@Service
public class ForumReplyServiceImpl extends ServiceImpl<ForumReplyMapper, ForumReply> implements ForumReplyService {
}
