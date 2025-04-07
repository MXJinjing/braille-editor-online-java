package wang.jinjing.editor.service.file;

import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.editor.pojo.VO.ObjectMetadataVO;

import java.io.InputStream;
import java.util.List;

public interface UserFileService {

    // filePath: end without '/'
    // folderPath: end with '/'
    // user-bucket: "user-" + currentUser.getUUID()

    void uploadFile(String path, MultipartFile file, boolean overwrite);

    void uploadFileByBytes(String path, byte[] bytes, boolean overwrite);

    InputStream downloadFile(String path);

    byte[] downloadAsBytes(String path);

    ObjectMetadataVO getByPath(String filePath);

    List<ObjectMetadataVO> listFiles(String folderPath);

    void moveToTrash(String path);

    void moveToTrash(List<String> paths);

    void recoveryFromTrash(List<Long> deleteIds);

    void realDeleteFiles(List<Long> deleteIds);

    void mkDir(String path, String folderName);

    void mkDirsWithParent(String pathWithFolderName);

}
