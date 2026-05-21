package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.entity.ActivityCheckin;

import java.util.List;

public interface ActivityCheckinService extends IService<ActivityCheckin> {
    List<ActivityCheckin> activityCheckins(Long activityId);
    void checkin(Long activityId, Long userId);
    void batchCheckin(Long activityId, List<Long> userIds);
    List<ActivityCheckin> mine();
}
