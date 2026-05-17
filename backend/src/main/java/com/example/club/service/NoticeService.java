package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    void publish(Notice notice);
    void updateNotice(Notice notice);
    void removeNotice(Long id);
    List<Notice> visibleNotices();
    List<Notice> managedNotices();
}
