package com.imooc.manus.api.infrastructure.repository;

import com.imooc.manus.api.domain.model.file.FileMeta;
import com.imooc.manus.api.domain.repository.FileRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的文件仓库实现（简化实现）。
 * 对应Python中的 SQLAlchemyFileRepository 类。
 * <p>
 * 在完整实现中，应替换为JPA持久化到files表。
 * </p>
 *
 * @author thezehui@gmail.com
 */
public class InMemoryFileRepository implements FileRepository {

    private final Map<String, FileMeta> store = new ConcurrentHashMap<>();

    @Override
    public void save(FileMeta file) {
        store.put(file.id(), file);
    }

    @Override
    public Optional<FileMeta> getById(String fileId) {
        return Optional.ofNullable(store.get(fileId));
    }
}

