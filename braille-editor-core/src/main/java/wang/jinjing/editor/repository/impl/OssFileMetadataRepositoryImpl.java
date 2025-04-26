package wang.jinjing.editor.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.OssFileMetadataMapper;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.repository.OssFileMetadataRepository;

import java.util.Date;
import java.util.List;

@Repository
public class OssFileMetadataRepositoryImpl extends
        AbstractBaseRepositoryImpl<OssFileMetadata, OssFileMetadataMapper> implements OssFileMetadataRepository {

    public OssFileMetadataRepositoryImpl(OssFileMetadataMapper mapper) {
        super(mapper);
    }

    @Override
    public OssFileMetadata selectByPath(String bucket, String path, FileTypeEnum fileType) {
        QueryWrapper<OssFileMetadata> wrapper = getWrapper(bucket, path, fileType);
        return mapper.selectOne(wrapper);
    }

    @Override
    public List<OssFileMetadata> listSoftDeleteItems(String bucketName, Sort sort) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("is_deleted",true);
        addSortToWrapper(wrapper, sort);
        return mapper.selectList(wrapper);
    }

    @Override
    public void setDeleteFlagByPath(String bucketName, String path, Date deleteAt, Long deleteBy) {
        UpdateWrapper<OssFileMetadata> wrapper = new UpdateWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("path", path)
                .eq("is_deleted", false)
                .set("is_deleted", true)
                .set("delete_at", deleteAt)
                .set("deleted_by",deleteBy);
        mapper.update(wrapper);
    }

    @Override
    public Page<OssFileMetadata> searchPage(
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
            Sort sort) {

        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();

        wrapper.ne("real_file_name","");
        wrapper.eq("is_deleted",false);

        if (s3Bucket != null) {
            wrapper.eq("s3_bucket", s3Bucket);
        }
        if (realFileName != null) {
            wrapper.like("real_file_name", realFileName);
        }
        if (path != null) {
            wrapper.like("path", path);
        }
        if (isDir != null) {
            wrapper.eq("is_dir", isDir);
        }
        if (createAtStart != null) {
            wrapper.ge("create_at", createAtStart);
        }
        if (createAtEnd != null) {
            wrapper.le("create_at", createAtEnd);
        }
        if (lastModifiedAtStart != null) {
            wrapper.ge("last_modified_at", lastModifiedAtStart);
        }
        if (lastModifiedAtEnd != null) {
            wrapper.le("last_modified_at", lastModifiedAtEnd);
        }
        if (createBy != null) {
            wrapper.eq("create_by", createBy);
        }
        if (lastModifiedBy != null) {
            wrapper.eq("last_modified_by", lastModifiedBy);
        }
        if (mimeType != null) {
            wrapper.likeRight("mime_type", mimeType);
        }

        // Add sorting
        addSortToWrapper(wrapper, sort);

        return mapper.selectPage(page, wrapper);
    }

    @Override
    public Page<OssFileMetadata> listSoftDeleteItemsPaged(String bucketName, Page<OssFileMetadata> page, Sort sort) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("is_deleted", true);
        addSortToWrapper(wrapper, sort);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public boolean existsByS3Key(String bucketName, String s3Key){
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("s3_key", s3Key)
                .eq("is_deleted", false);
        return mapper.exists(wrapper);
    }

    @Override
    public boolean existsByPath(String bucket, String newPath, FileTypeEnum fileType) {
        QueryWrapper<OssFileMetadata> wrapper = getWrapper(bucket, newPath, fileType);
        return mapper.exists(wrapper);
    }

    private QueryWrapper<OssFileMetadata> getWrapper(String bucket, String newPath, FileTypeEnum fileType) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucket)
                .eq("path", newPath)
                .eq("is_deleted", false);
        if(FileTypeEnum.FILE.equals(fileType)){
            wrapper.eq("is_dir", false);
        }else if (FileTypeEnum.FOLDER.equals(fileType)){
            wrapper.eq("is_dir", true);
        }
        return wrapper;
    }

    @Override
    public void clearBucket(String bucketName) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName);
        wrapper.ne("path", "/"); // 除去根目录
        mapper.delete(wrapper);
    }

    @Override
    public int deleteBucket(String bucketName){
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName);
        return mapper.delete(wrapper);
    }

    @Override
    public Page<OssFileMetadata> listPageByPath(String bucketName, String path, Page<OssFileMetadata> page, Sort sort) {
        MPJQueryWrapper<OssFileMetadata> wrapper = new MPJQueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("parent_path", path)
                .eq("is_deleted", false);
                ;
        addSortToWrapper(wrapper, sort);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<OssFileMetadata> listByPath(String bucketName, String path, Sort sort) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("parent_path", path)
                .eq("is_deleted", false);
        addSortToWrapper(wrapper,sort);
        return mapper.selectList(wrapper);
    }

    @Override
    public int realDeleteByPath(String bucket, String path) {
        MPJQueryWrapper<OssFileMetadata>  wrapper = new MPJQueryWrapper<>();
        wrapper.eq("s3_bucket", bucket)
                .eq("path", path)
                .eq("is_deleted", true);

        return mapper.deleteById(wrapper);
    }

    @Override
    public List<OssFileMetadata> listRecursiveByPath(String bucketName, String srcPath) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .likeRight("parent_path", srcPath);
        return mapper.selectList(wrapper);
    }


}
