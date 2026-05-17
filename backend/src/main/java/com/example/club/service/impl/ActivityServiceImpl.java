package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.common.PageResult;
import com.example.club.dto.ActivityQueryDTO;
import com.example.club.dto.ReviewDTO;
import com.example.club.entity.Activity;
import com.example.club.entity.ActivitySignup;
import com.example.club.entity.GroupMember;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.User;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.ActivityMapper;
import com.example.club.mapper.ActivitySignupMapper;
import com.example.club.mapper.GroupMemberMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.service.ActivityService;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {
    private final InterestGroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final ActivitySignupMapper signupMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<Activity> pageActivities(ActivityQueryDTO query, boolean publicOnly) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(query.getGroupId() != null, Activity::getGroupId, query.getGroupId())
                .like(query.getTitle() != null && !query.getTitle().isBlank(), Activity::getTitle, query.getTitle())
                .eq(query.getStatus() != null, Activity::getStatus, query.getStatus());
        if (publicOnly) {
            wrapper.eq(Activity::getStatus, 1)
                    .gt(Activity::getStartTime, LocalDateTime.now())
                    .orderByAsc(Activity::getStartTime);
        } else if (AuthContext.hasRole("ADMIN")) {
            wrapper.orderByDesc(Activity::getStartTime);
        } else {
            AuthContext.requireAny("LEADER");
            List<Long> groupIds = groupMapper.selectList(new LambdaQueryWrapper<InterestGroup>()
                            .eq(InterestGroup::getLeaderId, AuthContext.userId()))
                    .stream().map(InterestGroup::getId).toList();
            if (groupIds.isEmpty()) {
                return new PageResult<>(0, query.getPage(), query.getSize(), List.of());
            }
            wrapper.in(Activity::getGroupId, groupIds)
                    .orderByDesc(Activity::getStartTime);
        }
        Page<Activity> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public PageResult<Activity> visibleActivities(ActivityQueryDTO query) {
        Long userId = AuthContext.userId();
        List<Long> groupIds = groupMemberMapper.selectList(new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getStatus, 1))
                .stream().map(GroupMember::getGroupId).toList();
        if (groupIds.isEmpty()) {
            return new PageResult<>(0, query.getPage(), query.getSize(), List.of());
        }
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .in(Activity::getGroupId, groupIds)
                .eq(Activity::getStatus, 1)
                .gt(Activity::getStartTime, LocalDateTime.now())
                .orderByAsc(Activity::getStartTime);
        Page<Activity> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public void createActivity(Activity activity) {
        checkGroupOwner(activity.getGroupId());
        activity.setCreatorId(AuthContext.userId());
        activity.setCurrentParticipants(0);
        activity.setStatus(1);
        save(activity);
    }

    @Override
    public void updateActivity(Activity activity) {
        Activity old = getById(activity.getId());
        if (old == null) {
            throw new BusinessException("活动不存在");
        }
        checkGroupOwner(old.getGroupId());
        activity.setCreatorId(old.getCreatorId());
        activity.setCurrentParticipants(old.getCurrentParticipants());
        updateById(activity);
    }

    @Override
    public void deleteActivity(Long id) {
        Activity old = getById(id);
        if (old == null) {
            throw new BusinessException("活动不存在");
        }
        checkGroupOwner(old.getGroupId());
        removeById(id);
    }

    @Override
    public void signup(Long activityId) {
        AuthContext.requireAny("STUDENT");
        Activity activity = getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("活动不可报名");
        }
        if (!activity.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("活动已开始，不能继续报名");
        }
        Long userId = AuthContext.userId();
        ActivitySignup existing = signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId));
        if (existing != null && existing.getStatus() != null && existing.getStatus() != 2) {
            throw new BusinessException("已提交过该活动报名申请");
        }
        if (existing != null) {
            existing.setSignupTime(LocalDateTime.now());
            existing.setStatus(0);
            existing.setReviewerId(null);
            existing.setReviewRemark(null);
            existing.setReviewTime(null);
            signupMapper.updateById(existing);
            return;
        }
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setSignupTime(LocalDateTime.now());
        signup.setStatus(0);
        signupMapper.insert(signup);
    }

    @Override
    public void reviewSignup(Long signupId, ReviewDTO dto) {
        ActivitySignup signup = signupMapper.selectById(signupId);
        if (signup == null || signup.getStatus() == null || signup.getStatus() != 0) {
            throw new BusinessException("报名申请不存在或已审核");
        }
        Activity activity = getById(signup.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        checkGroupOwner(activity.getGroupId());
        if (dto.getStatus() != null && dto.getStatus() == 1) {
            if (!activity.getStartTime().isAfter(LocalDateTime.now())) {
                throw new BusinessException("活动已开始，不能通过报名");
            }
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                throw new BusinessException("活动报名人数已满");
            }
            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
            updateById(activity);
        } else if (dto.getStatus() == null || dto.getStatus() != 2) {
            throw new BusinessException("审核状态必须为通过或拒绝");
        }
        signup.setStatus(dto.getStatus());
        signup.setReviewerId(AuthContext.userId());
        signup.setReviewRemark(dto.getRemark());
        signup.setReviewTime(LocalDateTime.now());
        signupMapper.updateById(signup);
    }

    @Override
    public List<ActivitySignup> mySignups() {
        List<ActivitySignup> signups = signupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, AuthContext.userId())
                .orderByDesc(ActivitySignup::getSignupTime));
        signups.forEach(this::fillSignupNames);
        return signups;
    }

    @Override
    public List<ActivitySignup> signups(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        checkGroupOwner(activity.getGroupId());
        List<ActivitySignup> signups = signupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getActivityId, activityId));
        signups.forEach(this::fillSignupNames);
        return signups;
    }

    private void fillSignupNames(ActivitySignup signup) {
        signup.setUserName(userDisplayName(signup.getUserId()));
        signup.setReviewerName(userDisplayName(signup.getReviewerId()));
        Activity activity = getById(signup.getActivityId());
        signup.setActivityTitle(activity == null ? "未知活动" : activity.getTitle());
    }

    private String userDisplayName(Long userId) {
        if (userId == null) {
            return "-";
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "未知用户";
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

    private void checkGroupOwner(Long groupId) {
        AuthContext.requireAny("LEADER", "ADMIN");
        InterestGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("兴趣小组不存在");
        }
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能管理自己小组的活动");
        }
    }
}
