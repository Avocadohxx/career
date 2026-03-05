package com.career.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.career.backend.entity.Counselor;
import com.career.backend.mapper.CounselorMapper;
import com.career.backend.service.CounselorService;
import org.springframework.stereotype.Service;

@Service
public class CounselorServiceImpl extends ServiceImpl<CounselorMapper, Counselor> implements CounselorService {
}
