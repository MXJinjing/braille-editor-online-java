package wang.jinjing.editor.service.oss;

import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.pojo.VO.ObjectMetadataVO;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * 对象存储服务接口（Bucket名称通过实现类配置）
 */
public interface OssObjectService {

    void uploadBytes(String bucketName, String objectKey, byte[] data, Map<String, String> metadata) throws ObjectStorageException;

    void uploadStream(String bucketName, String objectKey, InputStream inputStream, long contentLength, Map<String, String> metadata) throws ObjectStorageException;

    byte[] downloadAsBytes(String bucketName, String objectKey) throws ObjectStorageException;

    InputStream downloadAsStream(String bucketName,String objectKey) throws ObjectStorageException;

    void deleteObject(String bucketName, String objectKey) throws ObjectStorageException;

    boolean objectExists(String bucketName, String objectKey) throws ObjectStorageException;

    String generatePresignedUrl(String bucketName, String objectKey, Duration expiration) throws ObjectStorageException;

    ObjectMetadataVO getObjectMetadata(String bucketName, String objectKey) throws ObjectStorageException;

    void copyObject(String sourceBucket, String sourcePath, String destBucket, String destPath);

    void deleteObjectsByPrefix(String bucket, String path);
}
