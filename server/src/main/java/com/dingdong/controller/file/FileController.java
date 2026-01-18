package com.dingdong.controller.file;

import com.dingdong.common.Result;
import com.dingdong.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 
 * @author Antigravity
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /** 允许的图片类型 */
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /** 最大文件大小 5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传头像
     * 
     * @param file 头像文件
     * @return 文件访问URL
     */
    @PostMapping("/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 校验文件
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过5MB");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (!isAllowedImageType(contentType)) {
            return Result.error("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        try {
            String url = fileStorageService.uploadFile(file);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);

            log.info("Avatar uploaded successfully: {}", url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Avatar upload failed", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否为允许的图片类型
     */
    private boolean isAllowedImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
