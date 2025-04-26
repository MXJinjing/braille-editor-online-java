package wang.jinjing.editor.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;

import java.util.Date;
import java.util.List;

public interface OssFileMetadataRepository extends BaseRepository<OssFileMetadata> {

    OssFileMetadata selectByPath(String bucket, String path, FileTypeEnum fileType);

    List<OssFileMetadata> listSoftDeleteItems(String bucketName, Sort sort);

    Page<OssFileMetadata> searchPage(
            String s3Bucket,
            String realFileName,
            String path,
            Boolean isDir,
            Date createAtStart,
            Date createAtEnd,
            Date lastModifiedAtStart,
            Date lastModifiedAtEnd,
            Long createBy,
            Long lastModifiedBy,
            String mimeType,
            Page<OssFileMetadata> page,
            Sort sort);

    Page<OssFileMetadata> listSoftDeleteItemsPaged(String bucketName, Page<OssFileMetadata> page, Sort sort);

    boolean existsByS3Key(String bucketName, String s3Key);

    boolean existsByPath(String bucket, String newPath, FileTypeEnum fileType);

    void clearBucket(String bucketName);

    int deleteBucket(String bucketName);

    Page<OssFileMetadata> listPageByPath(String bucketName, String path, Page<OssFileMetadata> page, Sort sort);

    List<OssFileMetadata> listByPath(String bucketName, String path, Sort sort);

    int realDeleteByPath(String destBucket, String destPath);

    List<OssFileMetadata> listRecursiveByPath(String bucketName, String srcPath);

    void setDeleteFlagByPath(String bucketName, String path, Date deleteAt, Long deleteBy);

}
