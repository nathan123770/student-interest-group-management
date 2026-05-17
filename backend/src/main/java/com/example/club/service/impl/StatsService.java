package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.entity.Activity;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.JoinApply;
import com.example.club.entity.User;
import com.example.club.mapper.ActivityMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.JoinApplyMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.utils.AuthContext;
import com.example.club.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final UserMapper userMapper;
    private final InterestGroupMapper groupMapper;
    private final ActivityMapper activityMapper;
    private final JoinApplyMapper applyMapper;

    public StatsVO overview() {
        AuthContext.requireAny("ADMIN");
        return new StatsVO(
                userMapper.selectCount(new LambdaQueryWrapper<User>()),
                groupMapper.selectCount(new LambdaQueryWrapper<InterestGroup>()),
                activityMapper.selectCount(new LambdaQueryWrapper<Activity>()),
                applyMapper.selectCount(new LambdaQueryWrapper<JoinApply>().eq(JoinApply::getStatus, 0))
        );
    }
}
