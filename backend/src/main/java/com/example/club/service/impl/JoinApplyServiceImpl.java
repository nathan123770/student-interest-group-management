package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.dto.JoinApplyDTO;
import com.example.club.dto.ReviewDTO;
import com.example.club.entity.GroupMember;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.JoinApply;
import com.example.club.entity.User;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.GroupMemberMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.JoinApplyMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.service.JoinApplyService;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JoinApplyServiceImpl extends ServiceImpl<JoinApplyMapper, JoinApply> implements JoinApplyService {
    private final InterestGroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    @Override
    public void apply(JoinApplyDTO dto) {
        AuthContext.requireAny("STUDENT");
        Long userId = AuthContext.userId();
        InterestGroup group = groupMapper.selectById(dto.getGroupId());
        if (group == null || group.getAuditStatus() != 1 || group.getStatus() != 1) {
            throw new BusinessException("兴趣小组不可申请");
        }
        Long memberCount = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, dto.getGroupId()).eq(GroupMember::getUserId, userId).eq(GroupMember::getStatus, 1));
        if (memberCount > 0) {
            throw new BusinessException("已是该小组成员");
        }
        Long applyCount = count(new LambdaQueryWrapper<JoinApply>().eq(JoinApply::getGroupId, dto.getGroupId()).eq(JoinApply::getUserId, userId).eq(JoinApply::getStatus, 0));
        if (applyCount > 0) {
            throw new BusinessException("已提交待审核申请，请勿重复申请");
        }
        JoinApply apply = new JoinApply();
        apply.setGroupId(dto.getGroupId());
        apply.setUserId(userId);
        apply.setReason(dto.getReason());
        apply.setStatus(0);
        save(apply);
    }

    @Override
    @Transactional
    public void review(Long applyId, ReviewDTO dto) {
        JoinApply apply = getById(applyId);
        if (apply == null || apply.getStatus() != 0) {
            throw new BusinessException("申请不存在或已审核");
        }
        InterestGroup group = groupMapper.selectById(apply.getGroupId());
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能审核自己小组的申请");
        }
        if (dto.getStatus() == 1) {
            if (group.getCurrentMembers() >= group.getMaxMembers()) {
                throw new BusinessException("小组人数已达上限");
            }
            Long memberCount = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, group.getId()).eq(GroupMember::getUserId, apply.getUserId()).eq(GroupMember::getStatus, 1));
            if (memberCount == 0) {
                GroupMember member = new GroupMember();
                member.setGroupId(group.getId());
                member.setUserId(apply.getUserId());
                member.setMemberRole("MEMBER");
                member.setJoinTime(LocalDateTime.now());
                member.setStatus(1);
                groupMemberMapper.insert(member);
                group.setCurrentMembers(group.getCurrentMembers() + 1);
                groupMapper.updateById(group);
            }
        }
        apply.setStatus(dto.getStatus());
        apply.setReviewerId(AuthContext.userId());
        apply.setReviewRemark(dto.getRemark());
        apply.setReviewTime(LocalDateTime.now());
        updateById(apply);
    }

    @Override
    public List<JoinApply> myApplies() {
        List<JoinApply> applies = list(new LambdaQueryWrapper<JoinApply>().eq(JoinApply::getUserId, AuthContext.userId()).orderByDesc(JoinApply::getCreateTime));
        applies.forEach(this::fillNames);
        return applies;
    }

    @Override
    public List<JoinApply> groupApplies(Long groupId) {
        InterestGroup group = groupMapper.selectById(groupId);
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能查看自己小组的申请");
        }
        List<JoinApply> applies = list(new LambdaQueryWrapper<JoinApply>().eq(JoinApply::getGroupId, groupId).orderByDesc(JoinApply::getCreateTime));
        applies.forEach(this::fillNames);
        return applies;
    }

    private void fillNames(JoinApply apply) {
        apply.setUserName(userDisplayName(apply.getUserId()));
        apply.setReviewerName(userDisplayName(apply.getReviewerId()));
        InterestGroup group = groupMapper.selectById(apply.getGroupId());
        apply.setGroupName(group == null ? "未知小组" : group.getName());
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
}
