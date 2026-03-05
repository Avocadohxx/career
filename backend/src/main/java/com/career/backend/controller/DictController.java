package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.backend.common.Result;
import com.career.backend.entity.Dict;
import com.career.backend.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/list")
    public Result<List<Dict>> list(@RequestParam(required = false) String dictType) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        if (dictType != null && !dictType.isBlank()) wrapper.eq(Dict::getDictType, dictType);
        wrapper.orderByAsc(Dict::getDictType).orderByAsc(Dict::getSort);
        return Result.success(dictService.list(wrapper));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Dict dict) {
        dictService.save(dict);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Dict dict) {
        dictService.updateById(dict);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        dictService.removeById(id);
        return Result.success(null);
    }
}
