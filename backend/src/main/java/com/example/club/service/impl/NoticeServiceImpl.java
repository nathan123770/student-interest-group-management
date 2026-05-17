package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.entity.GroupMember;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.Notice;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.GroupMemberMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.NoticeMapper;
import com.example.club.service.NoticeService;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    private final InterestGroupMapper groupMapper;
    private final GroupMemberMapper memberMapper;

    @Override
    public void publish(Notice notice) {
        checkWritable(notice);
        notice.setPublisherId(AuthContext.userId());
        notice.setStatus(1);
        notice.setTopFlag(notice.getTopFlag() == null ? 0 : notice.getTopFlag());
        save(notice);
    }

    @Override
    public void updateNotice(Notice notice) {
        Notice old = getById(notice.getId());
        if (old == null) {
            throw new BusinessException("公告不存在");
        }
        checkWritable(old);
        checkWritable(notice);
        notice.setPublisherId(old.getPublisherId());
        updateById(notice);
    }

    @Override
    public void removeNotice(Long id) {
        Notice old = getById(id);
        if (old == null) {
            throw new BusinessException("公告不存在");
        }
        checkWritable(old);
        removeById(id);
    }

    @Override
    public List<Notice> visibleNotices() {
        Long userId = AuthContext.userId();
        List<Long> groupIds = memberMapper.selectList(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getUserId, userId).eq(GroupMember::getStatus, 1))
                .stream().map(GroupMember::getGroupId).toList();
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .and(w -> w.eq(Notice::getNoticeType, "SYSTEM").or(groupIds.isEmpty(), x -> x.eq(Notice::getId, -1)).or(!groupIds.isEmpty(), x -> x.eq(Notice::getNoticeType, "GROUP").in(Notice::getGroupId, groupIds)))
                .orderByDesc(Notice::getTopFlag)
                .orderByDesc(Notice::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Notice> managedNotices() {
        if (AuthContext.hasRole("ADMIN")) {
            return list(new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreateTime));
        }
        AuthContext.requireAny("LEADER");
        List<Long> groupIds = groupMapper.selectList(new LambdaQueryWrapper<InterestGroup>()
                        .eq(InterestGroup::getLeaderId, AuthContext.userId()))
                .stream().map(InterestGroup::getId).toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getNoticeType, "GROUP")
                .in(Notice::getGroupId, groupIds)
                .orderByDesc(Notice::getTopFlag)
                .orderByDesc(Notice::getCreateTime));
    }

    private void checkWritable(Notice notice) {
        if ("SYSTEM".equals(notice.getNoticeType())) {
            AuthContext.requireAny("ADMIN");
            return;
        }
        AuthContext.requireAny("LEADER", "ADMIN");
        InterestGroup group = groupMapper.selectById(notice.getGroupId());
        if (group == null) {
            throw new BusinessException("公告所属小组不存在");
        }
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能管理自己小组的公告");
        }
    }
}
