package wang.jinjing.editor.service.file;

import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.editor.pojo.VO.S3ObjectMetadataVO;

import java.io.InputStream;
import java.util.List;

public interface TeamFileService {

    // filePath: end without '/'
    // folderPath: end with '/'
    // team-bucket: "team-" + currentUser.getUUID()

    void uploadFile(Long teamId, String path, MultipartFile file, boolean overwrite);

    void uploadFileByBytes(Long teamId, String path, byte[] bytes, boolean overwrite);

    InputStream downloadFile(Long teamId, String path);

    byte[] downloadAsBytes(Long teamId, String path);

    S3ObjectMetadataVO getByPath(Long teamId, String filePath);

    List<S3ObjectMetadataVO> listFiles(Long teamId, String folderPath);

    void moveToTrash(Long teamId, String path);

    void moveToTrash(Long teamId, List<String> paths);

    void recoveryFromTrash(Long teamId, List<Long> deleteIds);

    void realDeleteFiles(Long teamId, List<Long> deleteIds);

    void mkDir(Long teamId, String path, String folderName);

    void mkDirsWithParent(Long teamId, String pathWithFolderName);

}
