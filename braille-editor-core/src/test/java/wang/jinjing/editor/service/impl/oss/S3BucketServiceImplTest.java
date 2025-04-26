package wang.jinjing.editor.service.impl.oss;

import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wang.jinjing.editor.pojo.VO.S3ObjectMetadataVO;
import wang.jinjing.editor.service.oss.S3ObjectService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class S3BucketServiceImplTest {

    private static String bucketName = "test-bucket-55555";


    @Autowired
    private S3BucketServiceImpl ossBucketService;
    @Autowired
    private S3ObjectService s3ObjectService;


    @Test
    void bucketLife() throws InterruptedException {

        // 创建 桶对象
        if(ossBucketService.bucketExists(bucketName)) {
            ossBucketService.deleteBucket(bucketName);
        }

        ossBucketService.createBucket(bucketName);
        assertTrue(ossBucketService.bucketExists(bucketName));
        log.info("created bucket {}", bucketName);

        Thread.sleep(100);

        List<String> strings = ossBucketService.listBuckets();
        Set<String> collect = new HashSet<>(strings);
        assertTrue(collect.contains(bucketName));

        // 添加文件信息
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        s3ObjectService.uploadBytes(bucketName,"/test/123/456","Hello World".getBytes(), metadata);
        log.info("uploaded {} bytes", metadata.get("key1"));

        //检查文件信息
        S3ObjectMetadataVO objectMetadata = s3ObjectService.getObjectMetadata(bucketName, "/test/123/456");
        assertEquals("Hello World".length(), objectMetadata.getSize());
        assertEquals("value1",objectMetadata.getUserMetadata().getOrDefault("key1", ""));
        Iterable<Result<Item>> results = ossBucketService.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix("test/123").build());
        assertTrue(results.iterator().hasNext());
        try{
            String s = results.iterator().next().get().objectName();
            log.info("get object key: {}", s);
            Thread.sleep(100);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        if(true){
            return;
        }

        // 清空桶
        ossBucketService.clearBucket(bucketName);
        assertTrue(ossBucketService.bucketExists(bucketName));
        Iterable<Result<Item>> results2 = ossBucketService.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        assertFalse(results2.iterator().hasNext());
        log.info("clear bucket {}", bucketName);


        // 删除桶
        ossBucketService.deleteBucket(bucketName);
        assertFalse(ossBucketService.bucketExists(bucketName));
        log.info("deleted bucket {}", bucketName);

    }



}