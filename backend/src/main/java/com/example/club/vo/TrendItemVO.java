package com.example.club.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrendItemVO {
    private String date;
    private long groupCount;
    private long activityCount;
    private long applyCount;
    private long checkinCount;
}
