package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.GuidancePost;
import com.career.backend.service.GuidancePostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/guidance/post")
public class GuidancePostController {

    @Autowired
    private GuidancePostService guidancePostService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Integer counselorId) {
        LambdaQueryWrapper<GuidancePost> wrapper = new LambdaQueryWrapper<>();
        if (counselorId != null) wrapper.eq(GuidancePost::getCounselorId, counselorId);
        wrapper.orderByDesc(GuidancePost::getCreateAt);
        return Result.success(guidancePostService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<GuidancePost> getById(@PathVariable Integer id) {
        return Result.success(guidancePostService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody GuidancePost post) {
        post.setCreateAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        guidancePostService.save(post);
        return Result.success(null);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody GuidancePost post) {
        post.setUpdatedAt(LocalDateTime.now());
        guidancePostService.updateById(post);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        guidancePostService.removeById(id);
        return Result.success(null);
    }
}
