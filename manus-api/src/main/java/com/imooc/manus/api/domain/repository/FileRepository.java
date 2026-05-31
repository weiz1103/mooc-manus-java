package com.imooc.manus.api.domain.repository;

import com.imooc.manus.api.domain.model.file.FileMeta;

import java.util.Optional;

/**
 * 文件模型数据仓库。
 * @author zhuang03@qq.com
 * @date 2026-05-27 02:14:44
 */
public interface FileRepository {

    /**
     * 新增或更新文件信息
     *
     * @param file 文件信息
     */
    void save(FileMeta file);

    /**
     * 根据传递的文件id获取文件信息
     *
     * @param fileId 文件id
     * @return 文件信息（Optional）
     */
    Optional<FileMeta> getById(String fileId);
}

