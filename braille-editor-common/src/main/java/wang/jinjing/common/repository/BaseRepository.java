package wang.jinjing.common.repository;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BaseRepository<T extends BaseEntity> {

    // 常用方法
    List<T> listAll();

    Page<T> listPage(Page<T> page);

    // 代理BaseMapper的方法
    int insert(T entity);

    int deleteById(T entity);

    int deleteById(Long id);

    int delete(Wrapper<T> queryWrapper);

    int updateById(T entity);

    int update(T entity, Wrapper<T> updateWrapper);

    T selectById(Serializable id);

    List<T> selectByIds(Collection<? extends Serializable> idList);

    void selectByIds(Collection<? extends Serializable> idList, ResultHandler<T> resultHandler);

    Long selectCount(Wrapper<T> queryWrapper);

    List<T> selectList(Wrapper<T> queryWrapper);

    void selectList(Wrapper<T> queryWrapper, ResultHandler<T> resultHandler);

    List<T> selectList(IPage<T> page, Wrapper<T> queryWrapper);

    void selectList(IPage<T> page, Wrapper<T> queryWrapper, ResultHandler<T> resultHandler);

    List<Map<String, Object>> selectMaps(Wrapper<T> queryWrapper);

    void selectMaps(Wrapper<T> queryWrapper, ResultHandler<Map<String, Object>> resultHandler);

    List<Map<String, Object>> selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<T> queryWrapper);

    void selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<T> queryWrapper, ResultHandler<Map<String, Object>> resultHandler);

    <E> List<E> selectObjs(Wrapper<T> queryWrapper);

    <E> void selectObjs(Wrapper<T> queryWrapper, ResultHandler<E> resultHandler);

    boolean existsById(Long id);

    List<T> searchList(T t, Map<String,Object> args, Sort sort);


    Map<Integer, Boolean> insertBatch(List<T> t);

    Map<Long, Boolean> updateBatch(List<T> t);

    Map<Long, Boolean> deleteBatch(List<Long> ids);

    Page<T> searchPage(T t, Page<T> page, Map<String, Object> args, Sort sort);

    int updateSingle(Long id, String field, Object value);

    List<T> selectByIdsWithLock(List<Long> ids);

    boolean existsByIdWithLock(Long id);

    T selectByIdWithLock(Long id);
}

