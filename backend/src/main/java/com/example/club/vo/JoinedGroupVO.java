package com.example.club.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinedGroupVO {
    private Long id;
    private String name;
    private Long categoryId;
    private Long leaderId;
    private String coverUrl;
    private String description;
    private String location;
    private Integer maxMembers;
    private Integer currentMembers;
    private String leaderName;
    private String memberRole;
    private LocalDateTime joinTime;
}
