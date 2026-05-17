package com.example.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.club.common.Result;
import com.example.club.entity.GroupCategory;
import com.example.club.mapper.GroupCategoryMapper;
import com.example.club.utils.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final GroupCategoryMapper categoryMapper;

    @GetMapping
    public Result<List<GroupCategory>> list() {
        return Result.ok(categoryMapper.selectList(new LambdaQueryWrapper<GroupCategory>().orderByAsc(GroupCategory::getSortOrder)));
    }

    @PostMapping
    public Result<Void> create(@RequestBody GroupCategory category) {
        AuthContext.requireAny("ADMIN");
        categoryMapper.insert(category);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody GroupCategory category) {
        AuthContext.requireAny("ADMIN");
        categoryMapper.updateById(category);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AuthContext.requireAny("ADMIN");
        categoryMapper.deleteById(id);
        return Result.ok();
    }
}
