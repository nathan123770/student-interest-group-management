package com.example.club.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsVO {
    private long userCount;
    private long groupCount;
    private long activityCount;
    private long pendingApplyCount;
}
