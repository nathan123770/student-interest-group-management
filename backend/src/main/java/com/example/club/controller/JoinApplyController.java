package com.example.club.controller;

import com.example.club.common.Result;
import com.example.club.dto.JoinApplyDTO;
import com.example.club.dto.ReviewDTO;
import com.example.club.entity.JoinApply;
import com.example.club.service.JoinApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
public class JoinApplyController {
    private final JoinApplyService applyService;

    @PostMapping
    public Result<Void> apply(@RequestBody JoinApplyDTO dto) {
        applyService.apply(dto);
        return Result.ok();
    }

    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody ReviewDTO dto) {
        applyService.review(id, dto);
        return Result.ok();
    }

    @GetMapping("/mine")
    public Result<List<JoinApply>> mine() {
        return Result.ok(applyService.myApplies());
    }

    @GetMapping("/group/{groupId}")
    public Result<List<JoinApply>> group(@PathVariable Long groupId) {
        return Result.ok(applyService.groupApplies(groupId));
    }
}
