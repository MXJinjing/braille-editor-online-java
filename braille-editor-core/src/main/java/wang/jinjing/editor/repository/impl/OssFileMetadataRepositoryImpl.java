package wang.jinjing.editor.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.OssFileMetadataMapper;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.repository.OssFileMetadataRepository;

import java.util.List;

@Repository
public class OssFileMetadataRepositoryImpl extends
        AbstractBaseRepositoryImpl<OssFileMetadata, OssFileMetadataMapper> implements OssFileMetadataRepository {

    private final static String IN_TRASH_SQL = """
                                SELECT 1
                                FROM oss_file_recycle r
                                WHERE r.origin_file_id = m.id
                            """;

    public OssFileMetadataRepositoryImpl(OssFileMetadataMapper mapper) {
        super(mapper);
    }

    @Override
    public OssFileMetadata selectByPath(String bucket, String path, boolean isDir) {
        MPJQueryWrapper<OssFileMetadata> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("m")
                .selectAll(OssFileMetadata.class)
                .eq("m.s3_bucket", bucket)
//                .eq("m.s3_key", path)
                .eq("m.is_dir", isDir)
//                .notExists(IN_TRASH_SQL)
                ;
        return mapper.selectOne(wrapper);
    }

    @Override
    public OssFileMetadata selectByPathWithLock(String bucket, String path, boolean isDir) {
        MPJQueryWrapper<OssFileMetadata> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("m")
                .selectAll(OssFileMetadata.class)
                .eq("m.s3_bucket", bucket)
//                .eq("m.s3_key", path)
                .eq("m.is_dir", isDir)
//                .notExists(IN_TRASH_SQL)
                .last("FOR UPDATE");
        ;
        return mapper.selectOne(wrapper);
    }

    @Override
    public boolean existsByPath(String bucket, String newPath, boolean isDir) {
        MPJQueryWrapper<OssFileMetadata> wrapper = new MPJQueryWrapper<>();
        wrapper.setAlias("m")
                .selectAll(OssFileMetadata.class)
                .eq("m.s3_bucket", bucket)
//                .eq("m.s3_key", path)
                .eq("m.is_dir", isDir)
//                .notExists(IN_TRASH_SQL)
                .last("FOR UPDATE");

        return mapper.exists(wrapper);
    }

    @Override
    public Page<OssFileMetadata> listPageByPath(String bucketName, String path, Page<OssFileMetadata> page, Sort sort) {
        MPJQueryWrapper<OssFileMetadata> wrapper = new MPJQueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
//                .likeRight("s3_key", path)
                .eq("parent_path", path)
//                .notExists(IN_TRASH_SQL)
                ;
        addSortToWrapper(wrapper, sort);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<OssFileMetadata> listByPath(String bucketName, String path, Sort sort) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .eq("parent_path", path)
//                .notExists(IN_TRASH_SQL)
        ;
        addSortToWrapper(wrapper,sort);
        return mapper.selectList(wrapper);
    }

    @Override
    public int deleteByPath(String bucket, String path) {
        MPJQueryWrapper<OssFileMetadata>  wrapper = new MPJQueryWrapper<>();
        wrapper.eq("s3_bucket", bucket)
                .eq("s3_key", path);

        return mapper.deleteById(wrapper);
    }

    @Override
    public int updatePath(Long id, String newPath) {
        return updateSingle(id,"s3_key",newPath);
    }

    @Override
    public List<OssFileMetadata> listRecursiveByPath(String bucketName, String srcPath) {
        QueryWrapper<OssFileMetadata> wrapper = new QueryWrapper<>();
        wrapper.eq("s3_bucket", bucketName)
                .likeRight("parent_path", srcPath);
        return mapper.selectList(wrapper);
    }


}
