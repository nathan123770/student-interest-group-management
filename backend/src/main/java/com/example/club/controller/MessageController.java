package com.example.club.controller;

import com.example.club.common.Result;
import com.example.club.entity.Message;
import com.example.club.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/mine")
    public Result<List<Message>> mine() {
        return Result.ok(messageService.mine());
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        messageService.markAllRead();
        return Result.ok();
    }
}
