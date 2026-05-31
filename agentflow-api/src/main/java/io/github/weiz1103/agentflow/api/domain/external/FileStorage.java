package io.github.weiz1103.agentflow.api.domain.external;

import io.github.weiz1103.agentflow.api.domain.model.file.FileMeta;

import java.io.InputStream;

/**
 * 文件存储桶协议。
 * @author zhuang03@qq.com
 * @date 2026-05-30 01:21:08
 */
public interface FileStorage {

    /**
     * 根据传递的文件源上传文件后返回文件信息
     *
     * @param filename    文件。
     * @param inputStream 文件数据。
     * @param size        文件大小（字节）
     * @param mimeType    MIME类型
     * @return 文件信息
     */
    FileMeta uploadFile(String filename, InputStream inputStream, long size, String mimeType);

    /**
     * 根据传递的文件id下载文件，并返回文件。文件信息
     *
     * @param fileId 文件id
     * @return 文件数据流与文件信息的元。[InputStream, FileMeta]
     */
    Object[] downloadFile(String fileId);
}


