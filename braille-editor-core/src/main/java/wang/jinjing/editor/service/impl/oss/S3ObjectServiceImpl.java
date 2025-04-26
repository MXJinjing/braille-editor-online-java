package wang.jinjing.editor.service.impl.oss;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.pojo.VO.S3ObjectMetadataVO;
import wang.jinjing.editor.service.oss.S3ObjectService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class S3ObjectServiceImpl implements S3ObjectService {

    private final MinioClient minioClient;

    @Autowired
    public S3ObjectServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void uploadBytes(String bucketName, String objectKey, byte[] data, Map<String, String> metadata)
            throws ObjectStorageException {
        try (InputStream is = new ByteArrayInputStream(data)) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(is, data.length, -1)
                    .userMetadata(metadata)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new ObjectStorageException(ErrorEnum.UPLOAD_BYTES_FAILED);
        }
    }

    @Override
    public void uploadStream(String bucketName, String objectKey, InputStream inputStream,
                             long contentLength, Map<String, String> metadata)
            throws ObjectStorageException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, contentLength, -1)
                    .userMetadata(metadata)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new ObjectStorageException(ErrorEnum.UPLOAD_STREAM_FAILED);
        }
    }

    @Override
    public byte[] downloadAsBytes(String bucketName, String objectKey)
            throws ObjectStorageException {
        try (InputStream is = downloadAsStream(bucketName, objectKey)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new ObjectStorageException("Stream read error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ObjectStorageException(ErrorEnum.FILE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public InputStream downloadAsStream(String bucketName, String objectKey)
            throws ObjectStorageException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageException(ErrorEnum.FILE_DOWNLOAD_FAILED);
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey)
            throws ObjectStorageException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageException(ErrorEnum.FILE_DELETE_FAILED);
        }
    }

    @Override
    public boolean objectExists(String bucketName, String objectKey)
            throws ObjectStorageException {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new ObjectStorageException("Existence check failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ObjectStorageException("Existence check error: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String objectKey, Duration expiration)
            throws ObjectStorageException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry((int) expiration.getSeconds(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageException("Presigned URL generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public S3ObjectMetadataVO getObjectMetadata(String bucketName, String objectKey)
            throws ObjectStorageException {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );

            S3ObjectMetadataVO metadata = new S3ObjectMetadataVO();
            metadata.setContentType(response.contentType());
            metadata.setSize(response.size());
            metadata.setLastModified(response.lastModified());
            metadata.setUserMetadata(response.userMetadata());
            return metadata;
        } catch (Exception e) {
            throw new ObjectStorageException(ErrorEnum.OSS_GET_METADATA_FAILED);
        }
    }

    public void copyObject(String sourceBucket, String sourcePath,
                           String destBucket, String destPath)
            throws ObjectStorageException {

        try {
            // 构建复制源配置
            CopySource source = CopySource.builder()
                    .bucket(sourceBucket)
                    .object(sourcePath)
                    .build();

            // 构建复制操作参数
            CopyObjectArgs args = CopyObjectArgs.builder()
                    .source(source)
                    .bucket(destBucket)
                    .object(destPath)
                    .metadataDirective(Directive.COPY) // 保留原数据对象
                    .build();

            // 执行复制操作
            ObjectWriteResponse response = minioClient.copyObject(args);

            log.info("Copied object [{}] to [{}], versionId: {}",
                    sourceBucket + "/" + sourcePath,
                    destBucket + "/" + destPath,
                    response.versionId());

        } catch (Exception e) {
            throw new ObjectStorageException("Object copy failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteObjectsByPrefix(String bucket, String prefix) throws ObjectStorageException {
        try {
            // 1. 列出所有匹配前缀的对象
            List<DeleteObject> objectsToDelete = new ArrayList<>();
            Iterable<Result<Item>> listResults = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true) // 递归列出所有子对象
                            .build()
            );

            // 2. 收集要删除的对象列表
            for (Result<Item> result : listResults) {
                Item item = result.get();
                objectsToDelete.add(new DeleteObject(item.objectName()));
            }

            // 3. 批量删除对象
            if (!objectsToDelete.isEmpty()) {
                Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(bucket)
                                .objects(objectsToDelete)
                                .build()
                );

                // 4. 处理删除错误
                List<String> errorMessages = new ArrayList<>();
                for (Result<DeleteError> deleteResult : deleteResults) {
                    try {
                        DeleteError error = deleteResult.get();
                        errorMessages.add(String.format("Object: %s, Error: %s",
                                error.objectName(), error.message()));
                        log.error("Failed to delete object {}: {}",
                                error.objectName(), error.message());
                    } catch (Exception e) {
                        errorMessages.add("Error processing delete result: " + e.getMessage());
                    }
                }

                if (!errorMessages.isEmpty()) {
                    throw new ObjectStorageException("Partial deletion failures: " +
                            String.join("; ", errorMessages));
                }
            }
        } catch (ErrorResponseException e) {
            String msg = "Minio error: " + e.errorResponse().code() + " - " + e.getMessage();
            throw new ObjectStorageException(msg, e);
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to delete objects by prefix: " + e.getMessage(), e);
        }
    }


}