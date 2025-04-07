package wang.jinjing.editor.service.oss;

import wang.jinjing.editor.exception.ObjectStorageException;

import java.util.List;

/**
 * 对象存储服务接口（Bucket名称通过实现类配置）
 */
public interface OssBucketService {

    // 桶操作
    void createBucket(String bucketName) throws ObjectStorageException;

    void deleteBucket(String bucketName) throws ObjectStorageException;

    void clearBucket(String bucketName) throws ObjectStorageException;

    boolean bucketExists(String bucketName) throws ObjectStorageException;

    List<String> listBuckets() throws ObjectStorageException;

}
