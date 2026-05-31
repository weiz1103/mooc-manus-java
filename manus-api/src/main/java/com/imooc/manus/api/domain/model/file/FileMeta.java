package com.imooc.manus.api.domain.model.file;

import java.util.UUID;

/**
 * 文件信息Domain模型，用于记录Manus/Human上传or生成的文件。
 * <p>
 * 使用Java record保证不可变性。
 * </p>
 *
 * @param id        文件id
 * @param filename  文件名字
 * @param filepath  文件路径（沙箱中的绝对路径）
 * @param key       腾讯云COS中的存储路径key
 * @param extension 扩展名
 * @param mimeType  MIME类型
 * @param size      文件大小（字节）
 * @author zhuang03@qq.com
 * @date 2026-05-29 12:11:02
 */
public record FileMeta(
        String id,
        String filename,
        String filepath,
        String key,
        String extension,
        String mimeType,
        long size
) {
    /**
     * 默认构造：生成新的文件id，其余字段为空/0
     *
     * @return 空文件元信息
     */
    public static FileMeta empty() {
        return new FileMeta(UUID.randomUUID().toString(), "", "", "", "", "", 0);
    }

    /**
     * 仅包含文件路径的文件信息（沙箱结果附件场景使用）
     *
     * @param filepath 文件路径
     * @return FileMeta 实例
     */
    public static FileMeta ofFilepath(String filepath) {
        String normalized = filepath != null ? filepath.replace('\\', '/') : "";
        int lastSlash = normalized.lastIndexOf('/');
        String filename = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        int lastDot = filename.lastIndexOf('.');
        String extension = lastDot >= 0 && lastDot < filename.length() - 1
                ? filename.substring(lastDot + 1)
                : "";
        return new FileMeta(UUID.randomUUID().toString(), filename, filepath, "", extension, "", 0);
    }

    /**
     * 返回含更新filepath的新实例（record不可变，需复制）
     *
     * @param newFilepath 新的文件路径
     * @return 新的 FileMeta
     */
    public FileMeta withFilepath(String newFilepath) {
        return new FileMeta(id, filename, newFilepath, key, extension, mimeType, size);
    }
}

