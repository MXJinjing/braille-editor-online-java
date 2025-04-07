package wang.jinjing.editor.controller.manage;


import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.controller.RestfulAPIsController;
import wang.jinjing.editor.pojo.DTO.FileUploadBytesDTO;
import wang.jinjing.editor.pojo.VO.FileBytesDownloadVO;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.oss.OssBucketService;

import java.io.InputStream;
import java.util.Arrays;

@RestController
@RequestMapping("/api/manage/bucket")
public class OssBucketManageController {

    @Autowired
    private OssBucketService ossBucketService;


    @GetMapping("")
    public ResponseEntity<?> getAllBuckets() {
        return ResponseEntity.ok(ossBucketService.listBuckets());
    }

    @PutMapping("/{bucketName}")
    public ResponseEntity<?> createBucket(@PathVariable String bucketName) {
        ossBucketService.createBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucketName}/exists")
    public ResponseEntity<?> bucketExists(@PathVariable String bucketName) {
        return ResponseEntity.ok(ossBucketService.bucketExists(bucketName));
    }

    @DeleteMapping("/{bucketName}")
    public ResponseEntity<?> deleteBucket(@PathVariable String bucketName) {
        ossBucketService.deleteBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{bucketName}/clear")
    public ResponseEntity<?> clearBucket(@PathVariable String bucketName) {
        ossBucketService.clearBucket(bucketName);
        return ResponseEntity.ok().build();
    }


}
