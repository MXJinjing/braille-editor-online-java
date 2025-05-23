package wang.jinjing.common.repository;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractBaseRepositoryImpl<T extends BaseEntity, R extends BaseMapper<T>>
        implements BaseRepository<T> {
    protected final R mapper;

    public AbstractBaseRepositoryImpl(R mapper) {
        this.mapper = mapper;
    }

    // 常用方法
    @Override
    public List<T> listAll() {
        return mapper.selectList(null);
    }

    @Override
    public Page<T> listPage(Page<T> page) {
        return mapper.selectPage(page, null);
    }

    // 代理BaseMapper的方法
    @Override
    public int insert(T entity) {
        return mapper.insert(entity);
    }

    @Override
    public T selectByIdWithLock(Long id) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.last("FOR UPDATE");
        return mapper.selectOne(queryWrapper);
    }

    @Override
    public int deleteById(T entity) {
        return mapper.deleteById(entity);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }


    @Override
    public int delete(Wrapper<T> queryWrapper) {
        return mapper.delete(queryWrapper);
    }

    @Override
    public int updateById(T entity) {
        return mapper.updateById(entity);
    }

    @Override
    public int update(T entity, Wrapper<T> updateWrapper) {
        return mapper.update(entity, updateWrapper);
    }

    @Override
    public T selectById(Serializable id) {
        return mapper.selectById(id);
    }

    @Override
    public List<T> selectByIds(Collection<? extends Serializable> idList) {
        if(idList == null || idList.isEmpty()){
            return new ArrayList<>();
        }
        return mapper.selectByIds(idList);
    }

    @Override
    public void selectByIds(Collection<? extends Serializable> idList, ResultHandler<T> resultHandler) {
        mapper.selectByIds(idList, resultHandler);
    }

    @Override
    public Long selectCount(Wrapper<T> queryWrapper) {
        return mapper.selectCount(queryWrapper);
    }

    @Override
    public List<T> selectList(Wrapper<T> queryWrapper) {
        return mapper.selectList(queryWrapper);
    }

    @Override
    public void selectList(Wrapper<T> queryWrapper, ResultHandler<T> resultHandler) {
        mapper.selectList(queryWrapper, resultHandler);
    }

    @Override
    public List<T> selectList(IPage<T> page, Wrapper<T> queryWrapper) {
        return mapper.selectList(page, queryWrapper);
    }

    @Override
    public void selectList(IPage<T> page, Wrapper<T> queryWrapper, ResultHandler<T> resultHandler) {
        mapper.selectList(page, queryWrapper, resultHandler);
    }

    @Override
    public List<Map<String, Object>> selectMaps(Wrapper<T> queryWrapper) {
        return mapper.selectMaps(queryWrapper);
    }

    @Override
    public void selectMaps(Wrapper<T> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
        mapper.selectMaps(queryWrapper, resultHandler);
    }

    @Override
    public List<Map<String, Object>> selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<T> queryWrapper) {
        return mapper.selectMaps(page, queryWrapper);
    }

    @Override
    public void selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<T> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
        mapper.selectMaps(page, queryWrapper, resultHandler);
    }

    @Override
    public <E> List<E> selectObjs(Wrapper<T> queryWrapper) {
        return mapper.selectObjs(queryWrapper);
    }


    @Override
    public Map<Long, Boolean> updateBatch(List<T> t) {
        Map<Long,Boolean> result = new HashMap<Long,Boolean>();
        for (T t1 : t) {
            int i = updateById(t1);
            result.put(t1.getId(), i > 0);
        }
        return result;
    }

    @Override
    public boolean existsByIdWithLock(Long id) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BaseEntity::getId, id);
        queryWrapper.last("FOR UPDATE"); //加入悲观锁
        return mapper.selectCount(queryWrapper) > 0;
    }

    /**
     * @param ids
     * @return
     */
    @Override
    public Map<Long, Boolean> deleteBatch(List<Long> ids) {
        Map<Long,Boolean> result = new HashMap<Long,Boolean>();
        for (Long id : ids) {
            if(result.containsKey(id)){
                continue;
            }
            int i = deleteById(id);
            result.put(id, i > 0);
        }
        return result;
    }

    @Override
    public <E> void selectObjs(Wrapper<T> queryWrapper, ResultHandler<E> resultHandler) {
        mapper.selectObjs(queryWrapper, resultHandler);
    }

    @Override
    public List<T> selectByIdsWithLock(List<Long> ids) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(BaseEntity::getId, ids);
        queryWrapper.last("FOR UPDATE"); // 追加悲观锁
        return mapper.selectList(queryWrapper);
    }

    @Override
    public Map<Integer, Boolean> insertBatch(List<T> t) {
        Map<Integer,Boolean> result = new HashMap<Integer,Boolean>();
        for (int i = 0; i < t.size(); i++) {
            int insert = insert(t.get(i));
            result.put(i, insert > 0);
        }

        return result;
    }

    @Override
    public boolean existsById(Long id) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BaseEntity::getId, id);
        return mapper.exists(queryWrapper);
    }

    @Override
    public List<T> searchList(T t, Map<String,Object> args, Sort sort){
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(t);
        return mapper.selectList(queryWrapper);
    }

    @Override
    public Page<T> searchPage(T t, Page<T> page, Map<String, Object> args, Sort sort){
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(t);
        return (Page<T>) mapper.selectPage(page, queryWrapper);
    }


    @Override
    public int updateSingle(Long id, String field, Object value) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        wrapper.set(field, value);
        return mapper.update(null, wrapper);
    }


    protected void addSortToWrapper(QueryWrapper<T> wrapper, Sort sort){
        if(sort == null){
            return;
        }
        sort.forEach(order -> {
            if(order.isAscending()){
                wrapper.orderByAsc(order.getProperty());
            }else{
                wrapper.orderByDesc(order.getProperty());
            }
        });
    }

    protected void addSortToWrapper(MPJQueryWrapper<T> wrapper, Sort sort){
        if(sort == null){
            return;
        }
        sort.forEach(order -> {
            if(order.isAscending()){
                wrapper.orderByAsc(order.getProperty());
            }else{
                wrapper.orderByDesc(order.getProperty());
            }
        });
    }

    /**
     * 将实体字段名转换为数据库列名（需根据实际注解实现）
     */
    protected String getColumnName(Class<T> entityClass ,String property) {
        // 示例：假设使用 @TableField 注解，可通过反射获取列名
        try {
            Field field = entityClass.getDeclaredField(property);
            TableField tableField = field.getAnnotation(TableField.class);
            return tableField != null ? tableField.value() : property;
        } catch (NoSuchFieldException e) {
            return property; // 默认按属性名转下划线
        }
    }


}
