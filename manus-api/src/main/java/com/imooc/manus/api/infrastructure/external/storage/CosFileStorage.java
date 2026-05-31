package com.imooc.manus.api.infrastructure.external.storage;

import com.imooc.manus.api.domain.external.FileStorage;
import com.imooc.manus.api.domain.model.file.FileMeta;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;

/**
 * 基于腾讯云COS的文件存储实现。
 * @author zhuang03@qq.com
 * @date 2026-05-30 13:32:02
 */
public class CosFileStorage implements FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(CosFileStorage.class);

    private final COSClient cosClient;
    private final String bucket;
    private final String region;

    /**
     * 构造函数，完成COS客户端初始化
     *
     * @param secretId  腾讯云SecretId
     * @param secretKey 腾讯云SecretKey
     * @param region    COS Region
     * @param bucket    COS Bucket
     */
    public CosFileStorage(String secretId, String secretKey, String region, String bucket) {
        this.bucket = bucket;
        this.region = region;
        this.cosClient = new COSClient(
                new BasicCOSCredentials(secretId, secretKey),
                new ClientConfig(new Region(region))
        );
    }

    /**
     * 上传文件到COS
     *
     * @param filename    文件名
     * @param inputStream 文件流
     * @param size        文件大小
     * @param mimeType    MIME类型
     * @return 文件元信息
     */
    @Override
    public FileMeta uploadFile(String filename, InputStream inputStream, long size, String mimeType) {
        try {
            String fileId = UUID.randomUUID().toString();
            String extension = filename.contains(".")
                    ? filename.substring(filename.lastIndexOf(".") + 1) : "";
            String key = "uploads/" + fileId + "/" + filename;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size);
            if (mimeType != null) metadata.setContentType(mimeType);

            PutObjectRequest putRequest = new PutObjectRequest(bucket, key, inputStream, metadata);
            cosClient.putObject(putRequest);

            logger.info("文件[{}]上传到COS成功, key: {}", filename, key);
            return new FileMeta(fileId, filename, "", key, extension, mimeType != null ? mimeType : "", size);
        } catch (Exception e) {
            logger.error("上传文件到COS失败: {}", e.getMessage(), e);
            throw new RuntimeException("上传文件到COS失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从COS下载文件
     *
     * @param fileId 文件id
     * @return [inputStream, FileMeta]
     */
    @Override
    public Object[] downloadFile(String fileId) {
        throw new UnsupportedOperationException("downloadFile by id not supported - use key directly");
    }

    /**
     * 根据key从COS获取文件访问URL
     *
     * @param key COS对象key
     * @return 访问URL
     */
    public String getFileUrl(String key) {
        return "https://" + bucket + ".cos." + region + ".myqcloud.com/" + key;
    }
}

