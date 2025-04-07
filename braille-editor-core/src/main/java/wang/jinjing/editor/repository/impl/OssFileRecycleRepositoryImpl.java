package wang.jinjing.editor.repository.impl;

import org.springframework.stereotype.Repository;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.OssFileRecycleMapper;
import wang.jinjing.editor.pojo.entity.OssFileRecycle;
import wang.jinjing.editor.repository.OssFileRecycleRepository;

@Repository
public class OssFileRecycleRepositoryImpl
        extends AbstractBaseRepositoryImpl<OssFileRecycle, OssFileRecycleMapper> implements OssFileRecycleRepository {

    public OssFileRecycleRepositoryImpl(OssFileRecycleMapper mapper) {
        super(mapper);
    }
}
