package com.example.club.controller;

import com.example.club.common.Result;
import com.example.club.exception.BusinessException;
import com.example.club.utils.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @PostMapping("/image")
    public Result<Map<String, String>> image(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        AuthContext.requireAny("STUDENT", "LEADER", "ADMIN");
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp 格式图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("上传文件必须是图片");
        }
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return Result.ok(Map.of("url", baseUrl + "/uploads/" + filename));
    }
}
