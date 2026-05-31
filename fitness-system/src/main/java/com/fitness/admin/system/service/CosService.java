package com.fitness.admin.system.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Data
@Service
@ConfigurationProperties(prefix = "cos")
public class CosService {

    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String urlPrefix;

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        cosClient = new COSClient(credentials, clientConfig);
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }

    /**
     * 生成预签名上传URL
     *
     * @param filename 原始文件名
     * @param dir      目录前缀
     * @return [uploadUrl, fileUrl]
     */
    public String[] generatePresignedUrl(String filename, String dir) {
        String ext = "";
        if (filename != null && filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf("."));
        }
        String key = dir + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.PUT);
        // 签名有效期15分钟
        request.setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000));

        URL uploadUrl = cosClient.generatePresignedUrl(request);
        String fileUrl = urlPrefix + "/" + key;

        return new String[]{uploadUrl.toString(), fileUrl};
    }
}
