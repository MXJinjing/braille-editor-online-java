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

import java.io.InputStream;
import java.util.Arrays;

@RestController
@RequestMapping("/api/manage/file")
public class OssFileManageController {

    @Autowired
    private BaseFileService baseFileService;

    @PostMapping("/{bucketName}/upload")
    public ResponseEntity<?> upload(@NotBlank @PathVariable String bucketName,
                                    @Valid @RequestParam("file") MultipartFile file,
                                    @NotBlank @RequestParam("path") String path,
                                    @RequestParam(value = "overwrite", defaultValue = "false", required = false) Boolean overwrite ) {
        if(file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        baseFileService.uploadFile(bucketName, path, file, overwrite);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bucketName}/upload/bytes")
    public ResponseEntity<?> upload(@NotBlank @PathVariable String bucketName,
                                    @Valid @RequestParam("file") FileUploadBytesDTO file,
                                    @RequestParam(value = "overwrite", defaultValue = "false", required = false) Boolean overwrite ) {
        if(file.getContent().length == 0 || StrUtil.isBlank(file.getPath()) || StrUtil.isBlank(file.getFilename())){
            return ResponseEntity.badRequest().build();
        }

        String mimeType;
        if(StrUtil.isBlank(file.getMimeType())){
            mimeType = "text/plain";
        }else{
            mimeType = file.getMimeType();
        }

        baseFileService.uploadFileByBytes(bucketName,file.getPath(),file.getFilename(), mimeType, file.getContent() ,overwrite);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bucketName}/download")
    public ResponseEntity<?> download(@NotBlank @PathVariable String bucketName,
                                      @NotBlank @RequestParam String path){
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

    @GetMapping("/{bucketName}/download/bytes")
    public ResponseEntity<?> downloadBytes(@NotBlank @PathVariable String bucketName,
                                           @NotBlank @RequestParam String path){

        String fileName = path.substring(path.lastIndexOf("/")+1);
        String content = Arrays.toString(baseFileService.downloadAsBytes(bucketName, path));

        FileBytesDownloadVO fileBytesDownloadVO = new FileBytesDownloadVO();
        fileBytesDownloadVO.setContent(content);
        fileBytesDownloadVO.setFileName(fileName);

        return ResponseEntity.ok().body(fileBytesDownloadVO);
    }

    @GetMapping("/{bucketName}/metadata")
    public ResponseEntity<?> getMetadata(@NotBlank @PathVariable String bucketName,
                                         @NotBlank @RequestParam String path,
                                         @RequestParam(value = "isDir", defaultValue = "false", required = false) Boolean isDir){
        return ResponseEntity.ok(baseFileService.getMetadataByPath(bucketName, path, isDir));
    }

    @GetMapping("/{bucketName}/list")
    public ResponseEntity<?> listFiles(@NotBlank @PathVariable String bucketName,
                                       @NotBlank @RequestParam String folderPath,
                                       @RequestParam(value = "sort", defaultValue = "name", required = false) String sort){
        Sort parsedSort = RestfulAPIsController.getSortFromMap(sort);
        return ResponseEntity.ok(baseFileService.listFiles(bucketName, folderPath, parsedSort));
    }

    @GetMapping("/{bucketName}/list/paged")
    public ResponseEntity<?> listPagedFiles(@NotBlank @PathVariable String bucketName,
                                            @NotBlank @RequestParam String folderPath,
                                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                            @RequestParam(value = "sort", defaultValue = "name", required = false) String sort){
        Sort parsedSort = RestfulAPIsController.getSortFromMap(sort);
        return ResponseEntity.ok(baseFileService.listPagedFiles(bucketName, folderPath, page, size, parsedSort));
    }

    @PutMapping("/{bucketName}/delete")
    public ResponseEntity<?> moveToTrash(@NotBlank @PathVariable String bucketName,
                                         @NotBlank @RequestParam String path,
                                         @RequestParam(value = "isDir", defaultValue = "false", required = false) Boolean isDir){
        baseFileService.moveToTrash(bucketName, path, isDir);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bucketName}/moveToTrash/batch")
    public ResponseEntity<?> moveToTrash(@NotBlank @PathVariable String bucketName,
                                         @RequestParam(value = "paths") String[] paths,
                                         @RequestParam(value = "isDirs") Boolean[] isDirs){
        if(paths.length != isDirs.length){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(baseFileService.moveToTrash(bucketName, Arrays.asList(paths), Arrays.asList(isDirs)));
    }

    @PutMapping("/{bucketName}/recoveryFromTrash")
    public ResponseEntity<?> recoveryFromTrash(@NotBlank @PathVariable String bucketName,
                                               @RequestParam(value = "deleteIds") Long[] deleteIds){
        return ResponseEntity.ok(baseFileService.recoveryFromTrash(bucketName, Arrays.asList(deleteIds)));
    }

    @DeleteMapping("/{bucketName}/realDelete")
    public ResponseEntity<?> realDelete(@NotBlank @PathVariable String bucketName,
                                        @RequestParam(value = "deleteIds") Long[] deleteIds){
        return ResponseEntity.ok(baseFileService.realDeleteFiles(bucketName, Arrays.asList(deleteIds)));
    }

    @PostMapping("/{bucketName}/mkdir")
    public ResponseEntity<?> mkDir(@NotBlank @PathVariable String bucketName,
                                    @NotBlank @RequestParam String folderPath){
        return ResponseEntity.ok(baseFileService.mkDir(bucketName, folderPath));
    }

    @PostMapping("/{bucketName}/mkdirs")
    public ResponseEntity<?> mkDirsWithParent(@NotBlank @PathVariable String bucketName,
                                               @NotBlank @RequestParam String pathWithFolderName){
        return ResponseEntity.ok(baseFileService.mkDirsWithParent(bucketName, pathWithFolderName));
    }

    @PutMapping("/{bucketName}/copy")
    public ResponseEntity<?> copyObject(@NotBlank @PathVariable String bucketName,
                                        @NotBlank @RequestParam String sourcePath,
                                        @NotBlank @RequestParam String destBucket,
                                        @NotBlank @RequestParam String destPath,
                                        @RequestParam(value = "overwrite", defaultValue = "false", required = false) Boolean overwrite,
                                        @RequestParam(value = "isDir", defaultValue = "false", required = false) Boolean isDir){
        baseFileService.copyObject(bucketName, sourcePath, destBucket, destPath, overwrite, isDir);
        return ResponseEntity.ok().build();
    }


}
