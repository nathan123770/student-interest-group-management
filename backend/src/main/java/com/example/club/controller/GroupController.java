package com.example.club.controller;

import com.example.club.common.PageResult;
import com.example.club.common.Result;
import com.example.club.dto.GroupQueryDTO;
import com.example.club.entity.GroupMember;
import com.example.club.entity.InterestGroup;
import com.example.club.exception.BusinessException;
import com.example.club.service.InterestGroupService;
import com.example.club.utils.AuthContext;
import com.example.club.vo.JoinedGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final InterestGroupService groupService;

    @GetMapping("/public/page")
    public Result<PageResult<InterestGroup>> publicPage(GroupQueryDTO query) {
        return Result.ok(groupService.pageGroups(query, true));
    }

    @GetMapping("/page")
    public Result<PageResult<InterestGroup>> page(GroupQueryDTO query) {
        return Result.ok(groupService.pageGroups(query, false));
    }

    @GetMapping("/joined")
    public Result<List<JoinedGroupVO>> joined() {
        return Result.ok(groupService.joinedGroups());
    }

    @GetMapping("/{id}")
    public Result<InterestGroup> detail(@PathVariable Long id) {
        return Result.ok(groupService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody InterestGroup group) {
        groupService.createGroup(group);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody InterestGroup group) {
        groupService.updateGroup(group);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        InterestGroup group = groupService.getById(id);
        if (group == null) {
            throw new BusinessException("兴趣小组不存在");
        }
        if (!AuthContext.hasRole("ADMIN") && !group.getLeaderId().equals(AuthContext.userId())) {
            throw new BusinessException(403, "只能删除自己创建或负责的小组");
        }
        groupService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestParam Integer auditStatus) {
        groupService.reviewGroup(id, auditStatus);
        return Result.ok();
    }

    @GetMapping("/{id}/members")
    public Result<List<GroupMember>> members(@PathVariable Long id) {
        return Result.ok(groupService.members(id));
    }

    @PostMapping("/{id}/quit")
    public Result<Void> quit(@PathVariable Long id) {
        groupService.quit(id);
        return Result.ok();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return Result.ok();
    }
}
