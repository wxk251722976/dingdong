package com.dingdong.service.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 
 * @author Antigravity
 */
public interface FileStorageService {

    /**
     * 上传文件
     * 
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 删除文件
     * 
     * @param fileId 文件ID (格式: volumeId,fileId)
     * @return 是否删除成功
     */
    boolean deleteFile(String fileId);
}
