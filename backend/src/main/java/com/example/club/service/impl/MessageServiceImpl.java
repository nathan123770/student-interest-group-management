package com.example.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.club.entity.Message;
import com.example.club.exception.BusinessException;
import com.example.club.mapper.MessageMapper;
import com.example.club.service.MessageService;
import com.example.club.utils.AuthContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Override
    public void send(Long receiverId, String title, String content, String businessType, Long businessId) {
        if (receiverId == null) {
            return;
        }
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setReadStatus(0);
        save(message);
    }

    @Override
    public void sendBatch(Collection<Long> receiverIds, String title, String content, String businessType, Long businessId) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return;
        }
        receiverIds.stream().distinct().forEach(id -> send(id, title, content, businessType, businessId));
    }

    @Override
    public List<Message> mine() {
        return list(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, AuthContext.userId())
                .orderByAsc(Message::getReadStatus)
                .orderByDesc(Message::getCreateTime));
    }

    @Override
    public void markRead(Long id) {
        Message message = getById(id);
        if (message == null || !message.getReceiverId().equals(AuthContext.userId())) {
            throw new BusinessException("消息不存在");
        }
        message.setReadStatus(1);
        updateById(message);
    }

    @Override
    public void markAllRead() {
        List<Message> messages = list(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, AuthContext.userId())
                .eq(Message::getReadStatus, 0));
        messages.forEach(message -> message.setReadStatus(1));
        if (!messages.isEmpty()) {
            updateBatchById(messages);
        }
    }

    @Override
    public long unreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getReadStatus, 0));
    }
}
