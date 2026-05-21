package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.entity.Activity;
import com.example.club.entity.ActivityCheckin;
import com.example.club.entity.ActivitySignup;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.JoinApply;
import com.example.club.entity.Message;
import com.example.club.entity.OperationLog;
import com.example.club.entity.User;
import com.example.club.mapper.ActivityMapper;
import com.example.club.mapper.ActivityCheckinMapper;
import com.example.club.mapper.ActivitySignupMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.JoinApplyMapper;
import com.example.club.mapper.MessageMapper;
import com.example.club.mapper.OperationLogMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.utils.AuthContext;
import com.example.club.vo.StatsVO;
import com.example.club.vo.TrendItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final UserMapper userMapper;
    private final InterestGroupMapper groupMapper;
    private final ActivityMapper activityMapper;
    private final JoinApplyMapper applyMapper;
    private final ActivitySignupMapper signupMapper;
    private final ActivityCheckinMapper checkinMapper;
    private final MessageMapper messageMapper;
    private final OperationLogMapper logMapper;

    public StatsVO overview() {
        AuthContext.requireAny("ADMIN");
        Long currentUserId = AuthContext.userId();
        return new StatsVO(
                userMapper.selectCount(new LambdaQueryWrapper<User>()),
                groupMapper.selectCount(new LambdaQueryWrapper<InterestGroup>()),
                activityMapper.selectCount(new LambdaQueryWrapper<Activity>()),
                applyMapper.selectCount(new LambdaQueryWrapper<JoinApply>().eq(JoinApply::getStatus, 0)),
                signupMapper.selectCount(new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getStatus, 0)),
                messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, currentUserId)
                        .eq(Message::getReadStatus, 0)),
                checkinMapper.selectCount(new LambdaQueryWrapper<ActivityCheckin>().eq(ActivityCheckin::getStatus, 1)),
                logMapper.selectCount(new LambdaQueryWrapper<OperationLog>())
        );
    }

    public List<TrendItemVO> trends() {
        AuthContext.requireAny("ADMIN");
        List<TrendItemVO> items = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            items.add(new TrendItemVO(
                    date.toString(),
                    groupMapper.selectCount(new LambdaQueryWrapper<InterestGroup>()
                            .ge(InterestGroup::getCreateTime, start)
                            .lt(InterestGroup::getCreateTime, end)),
                    activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                            .ge(Activity::getCreateTime, start)
                            .lt(Activity::getCreateTime, end)),
                    applyMapper.selectCount(new LambdaQueryWrapper<JoinApply>()
                            .ge(JoinApply::getCreateTime, start)
                            .lt(JoinApply::getCreateTime, end)),
                    checkinMapper.selectCount(new LambdaQueryWrapper<ActivityCheckin>()
                            .ge(ActivityCheckin::getCheckinTime, start)
                            .lt(ActivityCheckin::getCheckinTime, end)
                            .eq(ActivityCheckin::getStatus, 1))
            ));
        }
        return items;
    }
}
