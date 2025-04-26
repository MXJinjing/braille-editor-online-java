package wang.jinjing.editor.controller.manage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.repository.OssFileMetadataRepository;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.oss.S3BucketService;

@RestController
@RequestMapping("/api/manage/bucket")
public class OssBucketManageController {

    @Autowired
    private S3BucketService s3BucketService;
    @Autowired
    private BaseFileService baseFileService;


    @GetMapping("")
    public ResponseEntity<?> getAllBuckets() {
        return ResponseEntity.ok(s3BucketService.listBuckets());
    }

    @PutMapping("/{bucketName}")
    public ResponseEntity<?> createBucket(@PathVariable String bucketName) {
        s3BucketService.createBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucketName}/init")
    public ResponseEntity<?> initBucket(@PathVariable String bucketName) {
        baseFileService.initBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucketName}/exists")
    public ResponseEntity<?> bucketExists(@PathVariable String bucketName) {
        return ResponseEntity.ok(s3BucketService.bucketExists(bucketName));
    }

    @DeleteMapping("/{bucketName}")
    public ResponseEntity<?> deleteBucket(@PathVariable  String bucketName) {
        baseFileService.deleteBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{bucketName}/clear")
    public ResponseEntity<?> clearBucket(@PathVariable String bucketName) {
        baseFileService.clearBucket(bucketName);
        return ResponseEntity.ok().build();
    }

}
