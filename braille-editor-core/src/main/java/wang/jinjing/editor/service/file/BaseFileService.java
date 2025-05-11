package wang.jinjing.editor.service.file;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
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

    void deleteFiles(String bucketName, String path );

    void recoveryFromTrash(String bucketName, Long recycleId);

    void realDeleteFiles(String bucketName, Long deleteId);

    long mkDirsWithParent(String bucketName, String pathWithFolderName, Long currentUserId);

    long mkDir(String bucketName, String folderPath, String folderName, Long currentUserId);

    OssFileMetadataVO moveObject(String bucketName, String srcPath, String destBucketName, String destPath, boolean createParent);

    OssFileMetadataVO copyObject(String bucketName, String srcPath, String destBucketName, String destPath, boolean createParent);

    void initBucket(String bucketName, EditorUser currentUser);

    OssFileMetadataVO rename(String bucketName, String oldPath, String newName);

    List<OssRecycleMetadataVO> listRecycleFiles(String bucketName, Sort sort);

    void deleteBucket(String bucketName);

    void clearBucket(String bucketName);

    String generateLink(String bucketName, String filePath);
}
