package wang.jinjing.editor.service.impl.oss;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.service.oss.S3BucketService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class S3BucketServiceImpl implements S3BucketService {

    @Autowired
    private MinioClient minioClient;

    private String POLICY_JSON = """
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Action": [
                            "s3:GetObject"
                        ],
                        "Effect": "Allow",
                        "Principal": {
                            "AWS": [
                                "*"
                            ]
                        },
                        "Resource": [
                            "arn:aws:s3:::%s/*"
                        ]
                    }
                ]
            }""";

    @Override
    public void createBucket(String bucketName) throws ObjectStorageException {
        try {
            if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                throw new ObjectStorageException("Bucket " + bucketName + " already exists.");
            }
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                            .bucket(bucketName).build();
            minioClient.makeBucket(makeBucketArgs);

            updateBucketPolicy(bucketName);

        }catch (Exception e) {
            throw new ObjectStorageException(e);
        }
    }

    @Override
    public void deleteBucket(String bucketName) throws ObjectStorageException {
        try {
            validateBucketExist(bucketName);
            deleteAllObjs(bucketName);
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        }catch (Exception e) {
            throw new ObjectStorageException(e);
        }
    }

    @Override
    public void clearBucket(String bucketName) throws ObjectStorageException {
        try {
            deleteAllObjs(bucketName);
            updateBucketPolicy(bucketName);
        } catch (Exception e){
            throw new ObjectStorageException(e);
        }
    }

    @Override
    public Iterable<Result<Item>> listObjects(ListObjectsArgs args){
        try {
            return minioClient.listObjects(args);
        } catch (Exception e) {
            throw new ObjectStorageException(e);
        }
    }

    private void deleteAllObjs(String bucketName) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        List<DeleteObject> objects = new ArrayList<>();
        Iterable<Result<Item>> results = listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            objects.add(new DeleteObject(Objects.requireNonNull(result.get()).objectName()));
        }

        if (!objects.isEmpty()) {
            Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build()
            );

            for (Result<DeleteError> errorResult : errors) {
                DeleteError error = errorResult.get();
                log.error("Delete object failed: {} - {}", error.objectName(), error.message());
            }
        }
    }

    @Override
    public boolean bucketExists(String bucketName) throws ObjectStorageException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new ObjectStorageException(e);
        }
    }

    @Override
    public List<String> listBuckets() throws ObjectStorageException {
        List<String> list = null;
        try {
            list = minioClient.listBuckets().stream().map(Bucket::name).toList();
        } catch (Exception e) {
            throw new ObjectStorageException(e);
        }
        return list;
    }

    private void updateBucketPolicy(String bucketName) throws Exception {
        String policy = String.format(POLICY_JSON, bucketName);
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
        );
    }

    private void validateBucketExist(String bucketName) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())){
            throw new ObjectStorageException(ErrorEnum.BUCKET_NOT_EXIST);
        }
    }
}
