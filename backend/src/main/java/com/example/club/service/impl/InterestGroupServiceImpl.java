package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.common.PageResult;
import com.example.club.dto.GroupQueryDTO;
import com.example.club.entity.GroupMember;
import com.example.club.entity.InterestGroup;
import com.example.club.entity.Role;
import com.example.club.entity.User;
import com.example.club.entity.UserRole;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.GroupMemberMapper;
import com.example.club.mapper.InterestGroupMapper;
import com.example.club.mapper.RoleMapper;
import com.example.club.mapper.UserMapper;
import com.example.club.mapper.UserRoleMapper;
import com.example.club.service.InterestGroupService;
import com.example.club.service.MessageService;
import com.example.club.service.OperationLogService;
import com.example.club.utils.AuthContext;
import com.example.club.vo.JoinedGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestGroupServiceImpl extends ServiceImpl<InterestGroupMapper, InterestGroup> implements InterestGroupService {
    private final GroupMemberMapper groupMemberMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final OperationLogService operationLogService;

    @Override
    public PageResult<InterestGroup> pageGroups(GroupQueryDTO query, boolean publicOnly) {
        LambdaQueryWrapper<InterestGroup> wrapper = new LambdaQueryWrapper<InterestGroup>()
                .like(query.getName() != null && !query.getName().isBlank(), InterestGroup::getName, query.getName())
                .eq(query.getCategoryId() != null, InterestGroup::getCategoryId, query.getCategoryId())
                .eq(query.getAuditStatus() != null, InterestGroup::getAuditStatus, query.getAuditStatus())
                .eq(query.getStatus() != null, InterestGroup::getStatus, query.getStatus())
                .orderByDesc(InterestGroup::getCreateTime);
        if (publicOnly) {
            wrapper.eq(InterestGroup::getAuditStatus, 1).eq(InterestGroup::getStatus, 1);
        } else if (AuthContext.hasRole("ADMIN")) {
            // 管理员查看全部小组，用于审核和系统管理。
        } else {
            AuthContext.requireAny("STUDENT", "LEADER");
            wrapper.eq(InterestGroup::getLeaderId, AuthContext.userId());
        }
        Page<InterestGroup> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        page.getRecords().forEach(this::fillLeaderName);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    @Transactional
    public void createGroup(InterestGroup group) {
        AuthContext.requireAny("STUDENT", "LEADER", "ADMIN");
        group.setLeaderId(AuthContext.userId());
        group.setCurrentMembers(AuthContext.hasRole("ADMIN") ? 1 : 0);
        group.setAuditStatus(AuthContext.hasRole("ADMIN") ? 1 : 0);
        group.setStatus(1);
        save(group);
        if (AuthContext.hasRole("ADMIN")) {
            ensureLeaderRole(group.getLeaderId());
            ensureLeaderMember(group);
        }
        operationLogService.record("GROUP", "CREATE", group.getId(), "创建小组：" + group.getName());
    }

    @Override
    public void updateGroup(InterestGroup group) {
        InterestGroup old = getById(group.getId());
        checkOwnerOrAdmin(old);
        group.setLeaderId(old.getLeaderId());
        group.setCurrentMembers(old.getCurrentMembers());
        updateById(group);
        operationLogService.record("GROUP", "UPDATE", group.getId(), "修改小组：" + group.getName());
    }

    @Override
    @Transactional
    public void reviewGroup(Long id, Integer auditStatus) {
        AuthContext.requireAny("ADMIN");
        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            throw new BusinessException("审核状态必须为通过或拒绝");
        }
        InterestGroup group = getById(id);
        if (group == null) {
            throw new BusinessException("兴趣小组不存在");
        }
        group.setAuditStatus(auditStatus);
        updateById(group);
        if (auditStatus != null && auditStatus == 1) {
            ensureLeaderRole(group.getLeaderId());
            ensureLeaderMember(group);
        }
        String statusText = auditStatus == 1 ? "通过" : "拒绝";
        messageService.send(group.getLeaderId(), "建组申请" + statusText,
                "你申请创建的“" + group.getName() + "”已审核" + statusText + "。",
                "GROUP_REVIEW", group.getId());
        operationLogService.record("GROUP", "REVIEW", group.getId(), "审核建组申请：" + statusText);
    }

    @Override
    public List<JoinedGroupVO> joinedGroups() {
        AuthContext.requireAny("STUDENT", "LEADER", "ADMIN");
        List<GroupMember> members = groupMemberMapper.selectList(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getUserId, AuthContext.userId())
                .eq(GroupMember::getStatus, 1)
                .orderByDesc(GroupMember::getJoinTime));
        return members.stream()
                .map(member -> {
                    InterestGroup group = getById(member.getGroupId());
                    if (group == null || group.getAuditStatus() == null || group.getAuditStatus() != 1
                            || group.getStatus() == null || group.getStatus() != 1) {
                        return null;
                    }
                    JoinedGroupVO vo = new JoinedGroupVO();
                    vo.setId(group.getId());
                    vo.setName(group.getName());
                    vo.setCategoryId(group.getCategoryId());
                    vo.setLeaderId(group.getLeaderId());
                    vo.setCoverUrl(group.getCoverUrl());
                    vo.setDescription(group.getDescription());
                    vo.setLocation(group.getLocation());
                    vo.setMaxMembers(group.getMaxMembers());
                    vo.setCurrentMembers(group.getCurrentMembers());
                    vo.setLeaderName(userDisplayName(group.getLeaderId()));
                    vo.setMemberRole(member.getMemberRole());
                    vo.setJoinTime(member.getJoinTime());
                    return vo;
                })
                .filter(item -> item != null)
                .toList();
    }

    @Override
    public List<GroupMember> members(Long groupId) {
        InterestGroup group = getById(groupId);
        checkOwnerOrAdmin(group);
        List<GroupMember> members = groupMemberMapper.selectList(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId).eq(GroupMember::getStatus, 1));
        members.forEach(member -> member.setUserName(userDisplayName(member.getUserId())));
        return members;
    }

    @Override
    public void quit(Long groupId) {
        Long userId = AuthContext.userId();
        GroupMember member = groupMemberMapper.selectOne(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId).eq(GroupMember::getStatus, 1));
        if (member == null) {
            throw new BusinessException("不是该小组成员");
        }
        if ("LEADER".equals(member.getMemberRole())) {
            throw new BusinessException("负责人不能直接退出自己负责的小组");
        }
        member.setStatus(0);
        groupMemberMapper.updateById(member);
        InterestGroup group = getById(groupId);
        group.setCurrentMembers(Math.max(0, group.getCurrentMembers() - 1));
        updateById(group);
    }

    @Override
    public void removeMember(Long groupId, Long userId) {
        InterestGroup group = getById(groupId);
        checkOwnerOrAdmin(group);
        GroupMember member = groupMemberMapper.selectOne(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId).eq(GroupMember::getStatus, 1));
        if (member == null || "LEADER".equals(member.getMemberRole())) {
            throw new BusinessException("成员不存在或不能移除负责人");
        }
        member.setStatus(0);
        groupMemberMapper.updateById(member);
        group.setCurrentMembers(Math.max(0, group.getCurrentMembers() - 1));
        updateById(group);
    }

    private void checkOwnerOrAdmin(InterestGroup group) {
        if (group == null) {
            throw new BusinessException("兴趣小组不存在");
        }
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能管理自己负责的小组");
        }
    }

    private void fillLeaderName(InterestGroup group) {
        if (group != null) {
            group.setLeaderName(userDisplayName(group.getLeaderId()));
        }
    }

    private String userDisplayName(Long userId) {
        if (userId == null) {
            return "待分配";
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

    private void ensureLeaderRole(Long userId) {
        Role leaderRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "LEADER"));
        if (leaderRole == null) {
            throw new BusinessException("LEADER角色不存在，请先初始化角色数据");
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, leaderRole.getId()));
        if (count == 0) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(leaderRole.getId());
            userRoleMapper.insert(userRole);
        }
    }

    private void ensureLeaderMember(InterestGroup group) {
        Long count = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, group.getId())
                .eq(GroupMember::getUserId, group.getLeaderId()));
        if (count == 0) {
            GroupMember member = new GroupMember();
            member.setGroupId(group.getId());
            member.setUserId(group.getLeaderId());
            member.setMemberRole("LEADER");
            member.setJoinTime(LocalDateTime.now());
            member.setStatus(1);
            groupMemberMapper.insert(member);
        }
        if (group.getCurrentMembers() == null || group.getCurrentMembers() < 1) {
            group.setCurrentMembers(1);
            updateById(group);
        }
    }
}
