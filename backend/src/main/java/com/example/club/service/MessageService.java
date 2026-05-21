package com.example.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.club.entity.Message;

import java.util.Collection;
import java.util.List;

public interface MessageService extends IService<Message> {
    void send(Long receiverId, String title, String content, String businessType, Long businessId);
    void sendBatch(Collection<Long> receiverIds, String title, String content, String businessType, Long businessId);
    List<Message> mine();
    void markRead(Long id);
    void markAllRead();
    long unreadCount(Long userId);
}
