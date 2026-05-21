package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.common.PageResult;
import com.example.club.dto.ActivityQueryDTO;
import com.example.club.dto.ReviewDTO;
import com.example.club.entity.Activity;
import com.example.club.entity.ActivityCheckin;
import com.example.club.entity.ActivitySignup;

import java.util.List;

public interface ActivityService extends IService<Activity> {
    PageResult<Activity> pageActivities(ActivityQueryDTO query, boolean publicOnly);
    PageResult<Activity> visibleActivities(ActivityQueryDTO query);
    void createActivity(Activity activity);
    void updateActivity(Activity activity);
    void deleteActivity(Long id);
    void signup(Long activityId);
    void reviewSignup(Long signupId, ReviewDTO dto);
    List<ActivitySignup> mySignups();
    List<ActivitySignup> signups(Long activityId);
    List<ActivityCheckin> checkins(Long activityId);
    void checkin(Long activityId, Long userId);
    void batchCheckin(Long activityId, List<Long> userIds);
    List<ActivityCheckin> myCheckins();
}
