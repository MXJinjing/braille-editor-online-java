package wang.jinjing.editor.service.impl.oss;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OssBucketServiceImplTest {

    private static String bucketName = "test-bucket-55555";


    @Autowired
    private OssBucketServiceImpl ossBucketService;


    @Test
    @Order(1)
    void createBucket() {
        long timestamp = System.currentTimeMillis();
        ossBucketService.createBucket(bucketName);
    }

    @Test
    @Order(2)
    void bucketExists() {
        assertTrue(ossBucketService.bucketExists(bucketName));
    }

    @Test
    @Order(3)
    void listBuckets() {
        List<String> strings = ossBucketService.listBuckets();
        Set<String> collect = new HashSet<>(strings);
        log.info(strings.toString());
        assertFalse(strings.isEmpty());
        assertTrue(collect.contains(bucketName));
    }

    @Test
    @Order(4)
    void deleteBucket() {
    }

    @Order(5)
    @Test
    void clearBucket() {

    }

    @Test
    @Order(6)
    void forceDeleteBucket() {
    }



}