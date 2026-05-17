package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.dto.JoinApplyDTO;
import com.example.club.dto.ReviewDTO;
import com.example.club.entity.JoinApply;

import java.util.List;

public interface JoinApplyService extends IService<JoinApply> {
    void apply(JoinApplyDTO dto);
    void review(Long applyId, ReviewDTO dto);
    List<JoinApply> myApplies();
    List<JoinApply> groupApplies(Long groupId);
}
