package com.example.club.service.impl;

import com.example.club.entity.Activity;
import com.example.club.entity.InterestGroup;
import com.example.club.mapper.ActivityMapper;
import com.example.club.mapper.ActivitySignupMapper;
import com.example.club.mapper.GroupMemberMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.service.ActivityCheckinService;
import com.example.club.service.MessageService;
import com.example.club.service.OperationLogService;
import com.example.club.utils.AuthContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private InterestGroupMapper groupMapper;
    @Mock
    private GroupMemberMapper groupMemberMapper;
    @Mock
    private ActivitySignupMapper signupMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ActivityCheckinService checkinService;
    @Mock
    private MessageService messageService;
    @Mock
    private OperationLogService operationLogService;

    @AfterEach
    void clearAuthContext() {
        AuthContext.clear();
    }

    @Test
    void deleteActivityMarksActivityDisabledInsteadOfRemovingIt() {
        ActivityServiceImpl service = new ActivityServiceImpl(
                groupMapper,
                groupMemberMapper,
                signupMapper,
                userMapper,
                checkinService,
                messageService,
                operationLogService
        );
        ReflectionTestUtils.setField(service, "baseMapper", activityMapper);

        Activity activity = new Activity();
        activity.setId(7L);
        activity.setGroupId(3L);
        activity.setTitle("Spring Boot 入门工作坊");
        activity.setStatus(1);

        InterestGroup group = new InterestGroup();
        group.setId(3L);
        group.setLeaderId(99L);

        AuthContext.set(1L, List.of("ADMIN"));
        when(activityMapper.selectById(7L)).thenReturn(activity);
        when(groupMapper.selectById(3L)).thenReturn(group);

        service.deleteActivity(7L);

        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).updateById(activityCaptor.capture());
        verify(activityMapper, never()).deleteById(any(Long.class));
        assertThat(activityCaptor.getValue().getId()).isEqualTo(7L);
        assertThat(activityCaptor.getValue().getStatus()).isZero();
    }
}
