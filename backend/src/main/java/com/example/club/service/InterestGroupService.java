package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.common.PageResult;
import com.example.club.dto.GroupQueryDTO;
import com.example.club.entity.GroupMember;
import com.example.club.entity.InterestGroup;
import com.example.club.vo.JoinedGroupVO;

import java.util.List;

public interface InterestGroupService extends IService<InterestGroup> {
    PageResult<InterestGroup> pageGroups(GroupQueryDTO query, boolean publicOnly);
    void createGroup(InterestGroup group);
    void updateGroup(InterestGroup group);
    void reviewGroup(Long id, Integer auditStatus);
    List<JoinedGroupVO> joinedGroups();
    List<GroupMember> members(Long groupId);
    void quit(Long groupId);
    void removeMember(Long groupId, Long userId);
}
