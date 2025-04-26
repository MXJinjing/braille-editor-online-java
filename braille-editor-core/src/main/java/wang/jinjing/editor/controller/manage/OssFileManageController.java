package wang.jinjing.editor.controller.manage;


import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.controller.RestfulAPIsController;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.editor.pojo.DTO.FileUploadBytesDTO;
import wang.jinjing.editor.pojo.VO.FileBytesDownloadVO;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.service.file.BaseFileService;

import java.io.InputStream;
import java.util.Arrays;

import static wang.jinjing.common.controller.RestfulAPIsController.getSortFromMap;

@RestController
@RequestMapping("/api/manage/file")
public class OssFileManageController {

    @Autowired
    private BaseFileService baseFileService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("bucket") String bucketName,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "fileName", required = false) String fileName,
                                    @RequestParam("path") String path) {
        if(file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        OssFileMetadataVO ossFileMetadataVO = baseFileService.uploadFile(bucketName, path, file);
        return ResponseEntity.ok().body(ossFileMetadataVO);
    }

    @PostMapping("/upload/bytes")
    public ResponseEntity<?> upload(@RequestParam("bucket") String bucketName,
                                    @RequestParam("file") FileUploadBytesDTO file) {
        if(file.getContent().length == 0 || StrUtil.isBlank(file.getPath()) || StrUtil.isBlank(file.getFilename())){
            return ResponseEntity.badRequest().build();
        }

        String mimeType;
        if(StrUtil.isBlank(file.getMimeType())){
            mimeType = "text/plain";
        }else{
            mimeType = file.getMimeType();
        }

        baseFileService.uploadFileByBytes(bucketName,file.getPath(),file.getFilename(), mimeType, file.getContent());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam("bucket") String bucketName,
                                      @RequestParam String path){
        InputStream fileStream = baseFileService.downloadFile(bucketName, path);
        InputStreamResource resource = new InputStreamResource(fileStream);

        String fileName = path.substring(path.lastIndexOf("/")+1);
        String contentType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)
                .toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/download/bytes")
    public ResponseEntity<?> downloadBytes(@RequestParam("bucket") String bucketName,
                                           @RequestParam String path){

        String fileName = path.substring(path.lastIndexOf("/")+1);
        String content = Arrays.toString(baseFileService.downloadAsBytes(bucketName, path));

        FileBytesDownloadVO fileBytesDownloadVO = new FileBytesDownloadVO();
        fileBytesDownloadVO.setContent(content);
        fileBytesDownloadVO.setFileName(fileName);

        return ResponseEntity.ok().body(fileBytesDownloadVO);
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> getMetadata(@RequestParam("bucket") String bucketName,
                                         @RequestParam String path,
                                         @RequestParam(value = "isDir", required = false) Boolean isDir
    ){
        FileTypeEnum fileTypeEnum = FileTypeEnum.ALL;
        if(isDir != null){
            fileTypeEnum = isDir?FileTypeEnum.FOLDER:FileTypeEnum.FILE;
        }
        return ResponseEntity.ok(baseFileService.getMetadataByPath(bucketName, path, fileTypeEnum));
    }

    @GetMapping("/list")
    public ResponseEntity<?> listFiles(@RequestParam("bucket") String bucketName,
                                       @RequestParam("path") String folderPath,
                                       @RequestParam(value = "sort", required = false) String sort){
        Sort parsedSort = Sort.unsorted();
        if(StrUtil.isNotBlank(sort)){
            parsedSort = getSortFromMap(sort);
        }
        return ResponseEntity.ok(baseFileService.listFiles(bucketName, folderPath, parsedSort));
    }

    @GetMapping("/list/paged")
    public ResponseEntity<?> listPagedFiles(@RequestParam("bucket") String bucketName,
                                            @RequestParam("path")  String folderPath,
                                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                            @RequestParam(value = "sort", defaultValue = "name", required = false) String sort){
        Sort parsedSort = getSortFromMap(sort);
        return ResponseEntity.ok(baseFileService.listPagedFiles(bucketName, folderPath, page, size, parsedSort));
    }

    @PutMapping("/delete")
    public ResponseEntity<?> moveToTrash(@RequestParam("bucket") String bucketName,
                                         @RequestParam String path){
        baseFileService.deleteFiles(bucketName, path);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/recoveryFromTrash")
    public ResponseEntity<?> recoveryFromTrash(@RequestParam("bucket") String bucketName,
                                               @RequestParam(value = "deleteId") Long deleteId){
        baseFileService.recoveryFromTrash(bucketName, deleteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/realDelete")
    public ResponseEntity<?> realDelete(@RequestParam("bucket") String bucketName,
                                        @RequestParam(value = "deleteId") Long deleteId){
        baseFileService.realDeleteFiles(bucketName, deleteId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mkdir")
    public ResponseEntity<?> mkDir(@RequestParam("bucket") String bucketName,
                                    @RequestParam String folderPath,
                                   @RequestParam String folderName){
        return ResponseEntity.ok(baseFileService.mkDir(bucketName, folderPath, folderName));
    }

    @PostMapping("/mkdirs")
    public ResponseEntity<?> mkDirsWithParent(@RequestParam("bucket") String bucketName,
                                               @RequestParam String pathWithFolderName){
        return ResponseEntity.ok(baseFileService.mkDirsWithParent(bucketName, pathWithFolderName));
    }

    @PutMapping("/copy")
    public ResponseEntity<?> copyObject(@RequestParam("bucket") String bucketName,
                                        @RequestParam String sourcePath,
                                        @RequestParam String destBucket,
                                        @RequestParam String destPath,
                                        @RequestParam(value = "overwrite", defaultValue = "false", required = false) Boolean overwrite){
        baseFileService.copyFile(bucketName, sourcePath, destBucket, destPath, overwrite);
        return ResponseEntity.ok().build();
    }


}
