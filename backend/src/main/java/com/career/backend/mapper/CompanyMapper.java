package com.career.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.career.backend.entity.Company;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}
