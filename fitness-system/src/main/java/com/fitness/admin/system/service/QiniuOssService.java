package com.fitness.admin.system.service;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Service
@ConfigurationProperties(prefix = "qiniu")
public class QiniuOssService {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;

    // 默认华东上传域名
    private String uploadDomain = "https://upload.qiniup.com";

    public Map<String, String> generateUploadCredential(String filename, String dir) {
        String ext = "";
        if (filename != null && filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf("."));
        }
        String key = dir + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        Auth auth = Auth.create(accessKey, secretKey);

        StringMap policy = new StringMap();
        // 返回 key 用于校验上传结果
        policy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":$(fsize)}");

        // 上传凭证有效期 30 分钟
        String uploadToken = auth.uploadToken(bucket, key, 1800, policy);

        String fileUrl = domain + "/" + key;

        Map<String, String> result = new HashMap<>();
        result.put("uploadToken", uploadToken);
        result.put("key", key);
        result.put("uploadDomain", uploadDomain);
        result.put("fileUrl", fileUrl);
        return result;
    }
}
