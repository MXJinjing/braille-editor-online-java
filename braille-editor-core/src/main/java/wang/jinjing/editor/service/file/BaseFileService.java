package wang.jinjing.editor.service.file;

import cn.hutool.core.lang.Editor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.VO.OssRecycleMetadataVO;
import wang.jinjing.editor.pojo.entity.EditorUser;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface BaseFileService {


    Page<OssFileMetadataVO> search(String bucket,
                                   String realFileName,
                                   String path,
                                   Boolean isDir,
                                   Date createAtStart, Date createAtEnd,
                                   Date lastModifiedAtStart, Date lastModifiedAtEnd,
                                   String createByUsername, String lastModifiedByUsername,
                                   String mimeType,
                                   Integer page, Integer size, Sort sort);

    OssFileMetadataVO uploadFile(String bucketName,  String path,  MultipartFile file);

    OssFileMetadataVO updateFile(String bucketName, String path, MultipartFile file);

    OssFileMetadataVO uploadFileByBytes(String bucketName, String path, String fileName, String mimeType, byte[] bytes);

    InputStream downloadFile(String bucketName, String path);

    byte[] downloadAsBytes(String bucketName, String path);

    OssFileMetadataVO getMetadataByPath(String bucketName, String filePath, FileTypeEnum file);

    boolean existsByPath(@NotBlank String bucketName, @NotBlank String filePath, FileTypeEnum fileType);

    List<OssFileMetadataVO> listFiles(@NotBlank String bucketName, @NotBlank String folderPath, Sort sort);

    Page<OssFileMetadataVO> listPagedFiles(@NotBlank String bucketName, @NotBlank String folderPath,int page, int size ,Sort sort);

    void softDeleteFile(String bucketName, String path);

    OssFileMetadataVO recoveryRecycleFile(String bucketName, Long recycleId);

    void deleteRecycleFile(String bucketName, Long deleteId);

    OssFileMetadataVO createFolder(String bucketName, String folderPath, String folderName);

    OssFileMetadataVO createFolderRecursive(String bucketName, String pathWithFolderName);

    OssFileMetadataVO moveFile(String bucketName, String srcPath, String destPath);

    OssFileMetadataVO copyFile(String sourceBucket, String sourcePath,
                               String destBucket, String destPath,
                               boolean overwrite);

    void initBucket(String bucketName, EditorUser user);

    OssFileMetadataVO rename(String bucketName, String oldPath, String newName);

    List<OssRecycleMetadataVO> listRecycleFiles(String bucketName, Sort sort);

    void deleteBucket(String bucketName);

    void clearBucket(String bucketName);

    String generateLink(String bucketName, String filePath);
}
