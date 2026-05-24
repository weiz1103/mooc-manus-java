package com.imooc.manus.api.domain.repository;

import com.imooc.manus.api.domain.model.file.FileMeta;

import java.util.Optional;

/**
 * 文件模型数据仓库。
 * 对应Python中的 FileRepository Protocol。
 *
 * @author thezehui@gmail.com
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

