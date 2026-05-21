package com.example.club.controller;

import com.example.club.common.Result;
import com.example.club.service.impl.StatsService;
import com.example.club.vo.StatsVO;
import com.example.club.vo.TrendItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<StatsVO> overview() {
        return Result.ok(statsService.overview());
    }

    @GetMapping("/trends")
    public Result<List<TrendItemVO>> trends() {
        return Result.ok(statsService.trends());
    }
}
