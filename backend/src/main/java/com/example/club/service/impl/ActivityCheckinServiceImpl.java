package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.entity.Activity;
import com.example.club.entity.ActivityCheckin;
import com.example.club.entity.ActivitySignup;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.User;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.ActivityCheckinMapper;
import com.example.club.mapper.ActivityMapper;
import com.example.club.mapper.ActivitySignupMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.service.ActivityCheckinService;
import com.example.club.service.OperationLogService;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityCheckinServiceImpl extends ServiceImpl<ActivityCheckinMapper, ActivityCheckin> implements ActivityCheckinService {
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper signupMapper;
    private final InterestGroupMapper groupMapper;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;

    @Override
    public List<ActivityCheckin> activityCheckins(Long activityId) {
        Activity activity = requireManagedActivity(activityId);
        List<ActivitySignup> approved = signupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getStatus, 1));
        return approved.stream().map(signup -> {
            ActivityCheckin checkin = getOne(new LambdaQueryWrapper<ActivityCheckin>()
                    .eq(ActivityCheckin::getActivityId, activityId)
                    .eq(ActivityCheckin::getUserId, signup.getUserId()), false);
            if (checkin == null) {
                checkin = new ActivityCheckin();
                checkin.setActivityId(activityId);
                checkin.setUserId(signup.getUserId());
                checkin.setStatus(0);
                checkin.setActivityTitle(activity.getTitle());
                checkin.setActivityStartTime(activity.getStartTime());
            }
            fillNames(checkin, activity);
            return checkin;
        }).toList();
    }

    @Override
    @Transactional
    public void checkin(Long activityId, Long userId) {
        Activity activity = requireManagedActivity(activityId);
        ActivitySignup signup = signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getStatus, 1));
        if (signup == null) {
            throw new BusinessException("只有报名审核通过的学生可以签到");
        }
        ActivityCheckin checkin = getOne(new LambdaQueryWrapper<ActivityCheckin>()
                .eq(ActivityCheckin::getActivityId, activityId)
                .eq(ActivityCheckin::getUserId, userId), false);
        if (checkin == null) {
            checkin = new ActivityCheckin();
            checkin.setActivityId(activityId);
            checkin.setUserId(userId);
            checkin.setCheckinMethod("MANUAL");
            checkin.setOperatorId(AuthContext.userId());
            checkin.setCheckinTime(LocalDateTime.now());
            checkin.setStatus(1);
            save(checkin);
        } else {
            checkin.setStatus(1);
            checkin.setCheckinMethod("MANUAL");
            checkin.setOperatorId(AuthContext.userId());
            checkin.setCheckinTime(LocalDateTime.now());
            updateById(checkin);
        }
        operationLogService.record("ACTIVITY", "CHECKIN", activityId, "活动签到：" + activity.getTitle());
    }

    @Override
    @Transactional
    public void batchCheckin(Long activityId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("请选择签到学生");
        }
        userIds.forEach(userId -> checkin(activityId, userId));
    }

    @Override
    public List<ActivityCheckin> mine() {
        Long userId = AuthContext.userId();
        List<ActivitySignup> approved = signupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getStatus, 1)
                .orderByDesc(ActivitySignup::getSignupTime));
        return approved.stream().map(signup -> {
            Activity activity = activityMapper.selectById(signup.getActivityId());
            ActivityCheckin checkin = getOne(new LambdaQueryWrapper<ActivityCheckin>()
                    .eq(ActivityCheckin::getActivityId, signup.getActivityId())
                    .eq(ActivityCheckin::getUserId, userId), false);
            if (checkin == null) {
                checkin = new ActivityCheckin();
                checkin.setActivityId(signup.getActivityId());
                checkin.setUserId(userId);
                checkin.setStatus(0);
            }
            fillNames(checkin, activity);
            return checkin;
        }).toList();
    }

    private Activity requireManagedActivity(Long activityId) {
        AuthContext.requireAny("LEADER", "ADMIN");
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        InterestGroup group = groupMapper.selectById(activity.getGroupId());
        if (group == null) {
            throw new BusinessException("兴趣小组不存在");
        }
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能管理自己小组的活动签到");
        }
        return activity;
    }

    private void fillNames(ActivityCheckin checkin, Activity activity) {
        checkin.setUserName(userDisplayName(checkin.getUserId()));
        if (activity != null) {
            checkin.setActivityTitle(activity.getTitle());
            checkin.setActivityStartTime(activity.getStartTime());
        }
    }

    private String userDisplayName(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "未知用户";
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }
}
