package wang.jinjing.editor.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;

import java.util.List;

public interface OssFileMetadataRepository extends BaseRepository<OssFileMetadata> {

    OssFileMetadata selectByPath(String bucket, String parentPath, boolean isDir);

    Page<OssFileMetadata> listPageByPath(String bucketName, String path, Page<OssFileMetadata> page, Sort sort);

    List<OssFileMetadata> listByPath(String bucketName, String path, Sort sort);

    int deleteByPath(String destBucket, String destPath);

    int updatePath(Long id, String newPath);

    List<OssFileMetadata> listRecursiveByPath(String bucketName, String srcPath);

    OssFileMetadata selectByPathWithLock(String sourceBucket, String sourcePath, boolean b);

    boolean existsByPath(String bucket, String newPath, boolean isDir);
}
