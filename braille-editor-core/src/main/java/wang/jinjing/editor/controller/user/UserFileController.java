package wang.jinjing.editor.controller.user;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.editor.pojo.DTO.OssFileMetadataSearchDTO;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.service.file.UserFileService;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import static wang.jinjing.common.controller.RestfulAPIsController.getSortFromMap;

@RestController
@RequestMapping("/api/file")
public class UserFileController {

    @Autowired
    private UserFileService userFileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("path") String path,
            @RequestParam("file")  MultipartFile file
            ) {
        OssFileMetadataVO vo = userFileService.uploadFile(path, file);
        return ResponseEntity.ok().body(vo);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateFile(
            @RequestParam("path") String path,
            @RequestParam("file") MultipartFile file) {
        OssFileMetadataVO vo = userFileService.updateFile(path, file);
        return ResponseEntity.ok().body(vo);
    }

    @PostMapping("/upload/bytes")
    public ResponseEntity<?> uploadFileByBytes(
            @RequestParam String path,
            @RequestParam String fileName,
            @RequestParam(required = false) String mimeType,
            @RequestParam byte[] bytes) {
        OssFileMetadataVO vo = userFileService.uploadFileByBytes(path, fileName, mimeType, bytes);
        return ResponseEntity.ok().body(vo);
    }

    @PostMapping("/new")
    public ResponseEntity<?> createEmptyFile(
            @RequestParam String path,
            @RequestParam("name") String fileName,
            @RequestParam(required = false) String mimeType
            ) {
        OssFileMetadataVO vo = userFileService.uploadFileByBytes(path, fileName, mimeType, new byte[0]);
        return ResponseEntity.ok().body(vo);
    }



    @GetMapping("/download")
    public void downloadFile(@RequestParam @NotNull String path, HttpServletResponse response) {
        try{
            OssFileMetadataVO metadata = userFileService.getByPath(path, FileTypeEnum.FILE);
            InputStream input = userFileService.downloadFile(path);
            byte buf[] = new byte[1024];
            int length = 0;
            response.reset();
            response.setHeader("Content-Type", metadata.getMimeType());
            String encodedFileName = URLEncoder.encode(metadata.getRealFileName(), "UTF-8").replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            response.setCharacterEncoding("UTF-8");
            OutputStream out = response.getOutputStream();

            while ((length = input.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
            out.flush();
            out.close();
        }catch (Exception e){
            throw new ServiceException(e);
        }
    }

    @GetMapping("/download/bytes")
    public ResponseEntity<byte[]> downloadAsBytes(@RequestParam String path) {
        byte[] bytes = userFileService.downloadAsBytes(path);
        return ResponseEntity.ok().body(bytes);
    }


    @GetMapping("/link")
    public ResponseEntity<?> generateLink(
            @RequestParam("path") String filePath) {
        String s = userFileService.generateLink(filePath);
        return ResponseEntity.ok().body(s);
    }


    @GetMapping("/metadata")
    public ResponseEntity<OssFileMetadataVO> getByPath(
            @RequestParam("path") String filePath,
            @RequestParam(value = "isDir", required = false) Boolean isDir) {
        FileTypeEnum fileTypeEnum = FileTypeEnum.ALL;
        if(isDir != null){
            fileTypeEnum = isDir?FileTypeEnum.FOLDER:FileTypeEnum.FILE;
        }
        OssFileMetadataVO vo = userFileService.getByPath(filePath, fileTypeEnum);
        return ResponseEntity.ok().body(vo);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OssFileMetadataVO>> listFiles(
            @RequestParam String folderPath,
            @RequestParam(required = false) String sorts) {
        Sort sort = Sort.by(defaultSort());
        if (sorts != null) {
            sort = getSortFromMap(sorts);
        }
        List<OssFileMetadataVO> vos = userFileService.listFiles(folderPath, sort);
        return ResponseEntity.ok().body(vos);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> moveToTrash(
            @RequestParam String path){
        userFileService.deleteFiles(path);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rename")
    public ResponseEntity<?> rename(
            @RequestParam String oldPath,
            @RequestParam String newName){
        OssFileMetadataVO rename = userFileService.rename(oldPath, newName);
        return StrUtil.isNotBlank(rename.getPath())? ResponseEntity.ok().body(rename): ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/copy")
    public ResponseEntity<?> copyObject(
            @RequestParam("src") String srcPath,
            @RequestParam("dest") String destPath,
            @RequestParam(defaultValue = "false",required = false) Boolean overwrite) {
        OssFileMetadataVO ossFileMetadataVO = userFileService.copyObject(srcPath, destPath, overwrite);
        return ResponseEntity.ok().body(ossFileMetadataVO);
    }

    @PutMapping("/move")
    public ResponseEntity<?> moveObjectRename(
            @RequestParam("src") String srcPath,
            @RequestParam("dest") String destPath) {
        OssFileMetadataVO ossFileMetadataVO = userFileService.moveObjectRename(srcPath, destPath);
        return ResponseEntity.ok().body(ossFileMetadataVO);
    }

    @PostMapping("/mkdirs")
    public ResponseEntity<?> mkDirsWithParent(@RequestParam String pathWithFolderName) {
        userFileService.mkDirsWithParent(pathWithFolderName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/init-bucket")
    public ResponseEntity<?> initBucket() {
        userFileService.initBucket();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/recoveryFromTrash")
    public ResponseEntity<?> recoveryFromTrash(@RequestParam Long deleteId) {
        userFileService.recoveryFromTrash(deleteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/realDelete")
    public ResponseEntity<?> realDeleteFiles(@RequestParam Long deleteId) {
        userFileService.realDeleteFiles(deleteId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mkdir")
    public ResponseEntity<?> mkDir(
            @RequestParam("path") String folderPath,
            @RequestParam("name") String folderName) {
        userFileService.mkDir(folderPath, folderName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(
            @RequestBody OssFileMetadataSearchDTO ossFileMetadataSearchDTO){
        Sort sort = Sort.by(defaultSort());
        if(ossFileMetadataSearchDTO != null) {
            if (ossFileMetadataSearchDTO.getSorts() != null) {
                // 分页参数
                sort = getSortFromMap(ossFileMetadataSearchDTO.getSorts());
            }
            Integer page = ossFileMetadataSearchDTO.getPage() == null? 1: ossFileMetadataSearchDTO.getPage();
            Integer size = ossFileMetadataSearchDTO.getSize() == null? 10 : ossFileMetadataSearchDTO.getSize();

            // 查询参数
            Page<OssFileMetadataVO> vos = userFileService.search(
                    ossFileMetadataSearchDTO.getRealFileName(),
                    ossFileMetadataSearchDTO.getPath(),
                    ossFileMetadataSearchDTO.getIsDir(),
                    ossFileMetadataSearchDTO.getCreateAtStart(),
                    ossFileMetadataSearchDTO.getCreateAtEnd(),
                    ossFileMetadataSearchDTO.getLastModifiedAtStart(),
                    ossFileMetadataSearchDTO.getLastModifiedAtEnd(),
                    ossFileMetadataSearchDTO.getMimeType(),
                    page, size, sort);
            return ResponseEntity.ok().body(vos);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private List<Sort.Order> defaultSort() {
        Sort.Order order1 = Sort.Order.desc("is_dir");
        Sort.Order order2 = Sort.Order.desc("real_file_name");
        return List.of(order1, order2);
    }
}