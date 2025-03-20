package wang.jinjing.editor.service.impl.manage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.entity.BaseEntity;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.common.service.BasicCRUDService;
import wang.jinjing.common.service.BatchCRUDService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractCRUDService<
         T extends BaseEntity,
         R extends BaseRepository<T>>
          implements BasicCRUDService<T>, BatchCRUDService<T> {

    protected final R repository;

    protected AbstractCRUDService(R repository) {
        this.repository = repository;
    }

    @Override
    public int addOne(T t) {
        return repository.insert(t);
    }

    @Override
    public T findById(Long id) {
        return repository.selectById(id);
    }

    @Override
    public int updateOne(Long id, T t) {
        if(!Objects.equals(t.getId(), id)){
            throw new IllegalArgumentException();
        }
        return repository.updateById(t);
    }

    @Override
    public int deleteOne(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public int addBatch(List<T> t) {
        return repository.insertBatch(t);
    }

    @Override
    public List<T> searchList(T t, Map<String,Object> args, Sort sort) {
        return repository.searchList(t, args, sort);
    }

    @Override
    public Page<T> searchPage(T t, Map<String,Object> args,  int page, int size, Sort sort) {
        return repository.searchPage(t, new Page<T>(page,size), args, sort);
    }

    @Override
    public int updateBatch(List<T> t) {
        return 0;
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return 0;
    }
}
