package wang.jinjing.editor.service.user;

import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.VO.OssRecycleMetadataVO;

import java.io.InputStream;
import java.util.List;

public interface UserFileService {

    // filePath: end without '/'
    // folderPath: end with '/'
    // user-bucket: "user-" + currentUser.getUUID()

    OssFileMetadataVO uploadFile(String path, MultipartFile file);

    OssFileMetadataVO updateFile(String path, MultipartFile file);

    OssFileMetadataVO uploadFileByBytes(String path, String fileName, String mimeType, byte[] bytes);

    InputStream downloadFile(String path);

    byte[] downloadAsBytes(String path);

    OssFileMetadataVO getByPath(String filePath, FileTypeEnum file);

    List<OssFileMetadataVO> listFiles(String folderPath, Sort sort);

    OssFileMetadataVO rename(String oldPath, String newName);

    void deleteFiles(String path);

    void recoveryFromTrash(Long deleteId);

    void realDeleteFiles(Long deleteId);

    void mkDir(String folderPath, String folderName);

    void mkDirsWithParent(String pathWithFolderName);

    void initBucket();

    OssFileMetadataVO moveObjectRename(String srcPath, String destPath);

    OssFileMetadataVO copyObject(String sourcePath, String destPath, boolean overwrite);

    List<OssRecycleMetadataVO> listRecycleFiles(Sort sort);

}
