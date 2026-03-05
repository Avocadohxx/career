package com.career.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.backend.common.Result;
import com.career.backend.entity.ForumPost;
import com.career.backend.entity.ForumReply;
import com.career.backend.service.ForumPostService;
import com.career.backend.service.ForumReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumPostService forumPostService;
    @Autowired
    private ForumReplyService forumReplyService;

    @GetMapping("/posts")
    public Result<?> posts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<ForumPost>()
                .orderByDesc(ForumPost::getCreateAt);
        return Result.success(forumPostService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/post/{id}")
    public Result<ForumPost> getPost(@PathVariable Integer id) {
        return Result.success(forumPostService.getById(id));
    }

    @PostMapping("/post/add")
    public Result<Void> addPost(@RequestBody ForumPost post) {
        post.setReplyCount(0);
        post.setCreateAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        forumPostService.save(post);
        return Result.success(null);
    }

    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Integer id) {
        forumPostService.removeById(id);
        forumReplyService.remove(new LambdaQueryWrapper<ForumReply>().eq(ForumReply::getPostId, id));
        return Result.success(null);
    }

    @GetMapping("/replies/{postId}")
    public Result<List<ForumReply>> replies(@PathVariable Integer postId) {
        return Result.success(forumReplyService.list(
                new LambdaQueryWrapper<ForumReply>()
                        .eq(ForumReply::getPostId, postId)
                        .orderByAsc(ForumReply::getCreateAt)));
    }

    @PostMapping("/reply/add")
    public Result<Void> addReply(@RequestBody ForumReply reply) {
        reply.setCreateAt(LocalDateTime.now());
        forumReplyService.save(reply);
        ForumPost post = forumPostService.getById(reply.getPostId());
        if (post != null) {
            post.setReplyCount(post.getReplyCount() + 1);
            forumPostService.updateById(post);
        }
        return Result.success(null);
    }
}
