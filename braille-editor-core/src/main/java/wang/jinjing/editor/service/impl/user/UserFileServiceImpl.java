package wang.jinjing.editor.service.impl.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.VO.OssRecycleMetadataVO;
import wang.jinjing.editor.service.AbstractUserService;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.file.UserFileService;
import wang.jinjing.editor.service.oss.S3BucketService;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Service
public class UserFileServiceImpl extends AbstractUserService implements UserFileService {

    @Autowired
    private S3BucketService bucketService;

    @Autowired
    private BaseFileService baseFileService;

    @NonNull
    private String getBucketName(){
        String bucketName = "user-"+ getCurrentUser().getId();
        return bucketName;
    }

    @Override
    public OssFileMetadataVO uploadFile(String path, MultipartFile file) {
        return baseFileService.uploadFile(getBucketName(),path,file);
    }

    @Override
    public OssFileMetadataVO updateFile(String path, MultipartFile file) {
        return baseFileService.updateFile(getBucketName(),path,file);
    }

    @Override
    public OssFileMetadataVO uploadFileByBytes(String path, String fileName, String mimeType, byte[] bytes) {
        return baseFileService.uploadFileByBytes(getBucketName(),path, fileName, mimeType, bytes);
    }

    @Override
    public InputStream downloadFile(String path) {
        InputStream inputStream = baseFileService.downloadFile(getBucketName(), path);
        return inputStream;
    }

    @Override
    public byte[] downloadAsBytes(String path) {
        return baseFileService.downloadAsBytes(getBucketName(),path);
    }

    @Override
    public OssFileMetadataVO getByPath(String filePath, FileTypeEnum file) {
        return baseFileService.getMetadataByPath(getBucketName(),filePath, file);
    }

    @Override
    public List<OssFileMetadataVO> listFiles(String folderPath, Sort sort) {
        return baseFileService.listFiles(getBucketName(),folderPath, sort);
    }

    @Override
    public OssFileMetadataVO rename(String oldPath, String newName){
        return baseFileService.rename(getBucketName(), oldPath, newName);
    }

    @Override
    public void deleteFiles(String path) {
        baseFileService.softDeleteFile(getBucketName(), path);
    }

    @Override
    public OssFileMetadataVO recoveryRecycleFile(Long deleteId) {
        return baseFileService.recoveryRecycleFile(getBucketName(), deleteId);
    }

    @Override
    public void deleteRecycleFile(Long deleteId) {
        baseFileService.deleteRecycleFile(getBucketName(), deleteId);
    }

    @Override
    public void createFolder(String folderPath, String folderName) {
        baseFileService.createFolder(getBucketName(),folderPath, folderName);
    }

    @Override
    public void createFolderRecursive(String pathWithFolderName) {
        baseFileService.createFolderRecursive(getBucketName(),pathWithFolderName);
    }

    @Override
    public void initBucket() {
        baseFileService.initBucket(getBucketName(), getCurrentUser());
    }

    @Override
    public OssFileMetadataVO moveObjectRename(String srcPath, String destPath) {
        return baseFileService.moveFile(getBucketName(),srcPath,destPath);
    }

    @Override
    public OssFileMetadataVO copyObject(String sourcePath, String destPath, boolean overwrite){
        String bucketName = getBucketName();
        return baseFileService.copyFile(bucketName, sourcePath, bucketName, destPath, overwrite);
    }

    @Override
    public List<OssRecycleMetadataVO> listRecycleFiles(Sort sort) {
        String bucketName = getBucketName();
        return baseFileService.listRecycleFiles(bucketName, sort);
    }

    @Override
    public Page<OssFileMetadataVO> search(String realFileName, String path, Boolean isDir, Date createAtStart, Date createAtEnd, Date lastModifiedAtStart, Date lastModifiedAtEnd, String mimeType, Integer page, Integer size, Sort sort) {
        return baseFileService.search(
                getBucketName(),
                realFileName,
                path,
                isDir,
                createAtStart,
                createAtEnd,
                lastModifiedAtStart,
                lastModifiedAtEnd,
                null,
                null,
                mimeType,
                page,
                size,
                sort
        );
    }

    @Override
    public String generateLink(String filePath) {
        return baseFileService.generateLink(getBucketName(),filePath);
    }

}
