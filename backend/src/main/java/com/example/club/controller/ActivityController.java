package com.example.club.controller;

import com.example.club.common.PageResult;
import com.example.club.common.Result;
import com.example.club.dto.ActivityQueryDTO;
import com.example.club.dto.ReviewDTO;
import com.example.club.entity.Activity;
import com.example.club.entity.ActivitySignup;
import com.example.club.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping("/public/page")
    public Result<PageResult<Activity>> publicPage(ActivityQueryDTO query) {
        return Result.ok(activityService.pageActivities(query, true));
    }

    @GetMapping("/page")
    public Result<PageResult<Activity>> page(ActivityQueryDTO query) {
        return Result.ok(activityService.pageActivities(query, false));
    }

    @GetMapping("/visible")
    public Result<PageResult<Activity>> visible(ActivityQueryDTO query) {
        return Result.ok(activityService.visibleActivities(query));
    }

    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        return Result.ok(activityService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Activity activity) {
        activityService.createActivity(activity);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Activity activity) {
        activityService.updateActivity(activity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return Result.ok();
    }

    @PostMapping("/{id}/signup")
    public Result<Void> signup(@PathVariable Long id) {
        activityService.signup(id);
        return Result.ok();
    }

    @GetMapping("/signups/mine")
    public Result<List<ActivitySignup>> mySignups() {
        return Result.ok(activityService.mySignups());
    }

    @PostMapping("/signups/{signupId}/review")
    public Result<Void> reviewSignup(@PathVariable Long signupId, @RequestBody ReviewDTO dto) {
        activityService.reviewSignup(signupId, dto);
        return Result.ok();
    }

    @GetMapping("/{id}/signups")
    public Result<List<ActivitySignup>> signups(@PathVariable Long id) {
        return Result.ok(activityService.signups(id));
    }
}
