package wang.jinjing.editor.service.impl.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.editor.pojo.VO.ObjectMetadataVO;
import wang.jinjing.editor.repository.OssFileMetadataRepository;
import wang.jinjing.editor.repository.OssFileRecycleRepository;
import wang.jinjing.editor.service.file.UserFileService;

import java.io.InputStream;
import java.util.List;

@Service
public class UserFileServiceImpl implements UserFileService {

    @Autowired
    private OssFileMetadataRepository ossFileMetadataRepository;

    @Autowired
    private OssFileRecycleRepository ossFileRecycleRepository;



    @Override
    public void uploadFile(String path, MultipartFile file, boolean overwrite) {

    }

    @Override
    public void uploadFileByBytes(String path, byte[] bytes, boolean overwrite) {

    }

    @Override
    public InputStream downloadFile(String path) {
        return null;
    }

    @Override
    public byte[] downloadAsBytes(String path) {
        return new byte[0];
    }

    @Override
    public ObjectMetadataVO getByPath(String filePath) {
        return null;
    }

    @Override
    public List<ObjectMetadataVO> listFiles(String folderPath) {
        return List.of();
    }

    @Override
    public void moveToTrash(String path) {

    }

    @Override
    public void moveToTrash(List<String> paths) {

    }

    @Override
    public void recoveryFromTrash(List<Long> deleteIds) {

    }

    @Override
    public void realDeleteFiles(List<Long> deleteIds) {

    }

    @Override
    public void mkDir(String path, String folderName) {

    }

    @Override
    public void mkDirsWithParent(String pathWithFolderName) {

    }
}
