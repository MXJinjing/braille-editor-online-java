package wang.jinjing.editor.service.file;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface BaseFileService {

    void uploadFile(String bucketName, String path, MultipartFile file, boolean overwrite);

    void uploadFileByBytes(String bucketName, String path,
                           String fileName, String mimeType, byte[] bytes, boolean overwrite) throws ObjectStorageException;

    InputStream downloadFile(String bucketName, String path);

    byte[] downloadAsBytes(String bucketName, String path);

    OssFileMetadataVO getMetadataByPath(String bucketName, String filePath, boolean isDir);

    boolean existsByPath(@NotBlank String bucketName, @NotBlank String filePath, boolean isDir);

    List<OssFileMetadataVO> listFiles(@NotBlank String bucketName, @NotBlank String folderPath, Sort sort);

    Page<OssFileMetadataVO> listPagedFiles(@NotBlank String bucketName, @NotBlank String folderPath,int page, int size ,Sort sort);

    void moveToTrash(String bucketName, String path, Boolean isDir);

    Map<String, ErrorEnum> moveToTrash(String bucketName, List<String> paths, List<Boolean> isDirs);

    Map<Long, ErrorEnum> recoveryFromTrash(String bucketName, List<Long> deleteIds);

    Map<Long, ErrorEnum> realDeleteFiles(String bucketName, List<Long> deleteIds);

    long mkDir(String bucketName, String folderPath);

    long mkDirsWithParent(String bucketName, String pathWithFolderName);

    void copyObject(String sourceBucket, String sourcePath,
                    String destBucket, String destPath,
                    boolean overwrite, boolean isDir);
}
