package com.example.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.common.Result;
import com.example.club.entity.Notice;
import com.example.club.service.NoticeService;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("/public/system")
    public Result<List<Notice>> publicSystem() {
        return Result.ok(noticeService.list(new LambdaQueryWrapper<Notice>().eq(Notice::getNoticeType, "SYSTEM").eq(Notice::getStatus, 1).orderByDesc(Notice::getTopFlag).orderByDesc(Notice::getCreateTime)));
    }

    @GetMapping("/visible")
    public Result<List<Notice>> visible() {
        return Result.ok(noticeService.visibleNotices());
    }

    @GetMapping
    public Result<List<Notice>> all() {
        AuthContext.requireAny("ADMIN");
        return Result.ok(noticeService.list(new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreateTime)));
    }

    @GetMapping("/managed")
    public Result<List<Notice>> managed() {
        return Result.ok(noticeService.managedNotices());
    }

    @PostMapping
    public Result<Void> publish(@RequestBody Notice notice) {
        noticeService.publish(notice);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Notice notice) {
        noticeService.updateNotice(notice);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.removeNotice(id);
        return Result.ok();
    }
}
