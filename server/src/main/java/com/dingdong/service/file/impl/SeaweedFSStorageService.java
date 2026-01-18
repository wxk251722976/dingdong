package com.dingdong.service.file.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dingdong.config.SeaweedFSProperties;
import com.dingdong.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * SeaweedFS 文件存储服务实现
 * 
 * @author Antigravity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeaweedFSStorageService implements FileStorageService {

    private final SeaweedFSProperties properties;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 1. 从 Master 获取文件ID和上传地址
            String assignUrl = properties.getMasterUrl() + "/dir/assign";
            Request assignRequest = new Request.Builder()
                    .url(assignUrl)
                    .get()
                    .build();

            String assignJson;
            try (Response response = httpClient.newCall(assignRequest).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.error("Failed to get file ID from SeaweedFS master");
                    throw new RuntimeException("获取文件ID失败");
                }
                assignJson = response.body().string();
            }

            JSONObject assignResult = JSONUtil.parseObj(assignJson);
            String fid = assignResult.getStr("fid");
            // 注意：master返回的url可能是Docker内部网络地址，需要使用配置的volumeUrl
            // String volumeUrl = assignResult.getStr("url"); // 不使用这个

            if (fid == null) {
                log.error("Invalid assign response: {}", assignJson);
                throw new RuntimeException("SeaweedFS 返回数据异常");
            }

            // 2. 使用配置的volume URL上传文件
            String uploadUrl = properties.getVolumeUrl() + "/" + fid;

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "file";
            }

            RequestBody fileBody = RequestBody.create(
                    file.getBytes(),
                    MediaType
                            .parse(file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", originalFilename, fileBody)
                    .build();

            Request uploadRequest = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            try (Response uploadResponse = httpClient.newCall(uploadRequest).execute()) {
                if (!uploadResponse.isSuccessful()) {
                    log.error("Failed to upload file to SeaweedFS volume: {}", uploadResponse.code());
                    throw new RuntimeException("文件上传失败");
                }
            }

            // 3. 返回公网访问URL
            String publicUrl = properties.getPublicUrl() + "/" + fid;
            log.info("File uploaded successfully: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("File upload error", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileId) {
        try {
            String deleteUrl = properties.getVolumeUrl() + "/" + fileId;
            Request request = new Request.Builder()
                    .url(deleteUrl)
                    .delete()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            log.error("File delete error", e);
            return false;
        }
    }
}
