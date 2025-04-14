package wang.jinjing.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.entity.BaseEntity;
import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.common.util.BeanConvertUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractCRUDService<
         T extends BaseEntity,
         VO extends BaseVO,
         R extends BaseRepository<T>>
          implements BasicCRUDService<T, VO>, BatchCRUDService<T, VO> {

    protected final R repository;

    protected final Class<VO> voClass;

    protected AbstractCRUDService(R repository, Class<VO> voClass) {
        this.repository = repository;
        this.voClass = voClass;
    }

    @Override
    public long addOne(T t) {
        int insert = repository.insert(t);
        return t.getId();
    }

    @Override
    public VO findById(Long id) {
        T t = repository.selectById(id);
        return BeanConvertUtil.convertToVo(voClass, t);
    }

    @Override
    public int updateOne(Long id, T t) {
        if(id == null){
            throw new IllegalArgumentException();
        }
        t.setId(id);
        return repository.updateById(t);
    }

    @Override
    public int deleteOne(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public List<VO> searchList(T t, Map<String,Object> args, Sort sort) {
        List<T> ts = repository.searchList(t, args, sort);
        return BeanConvertUtil.convertToVoList(voClass, ts);
    }

    @Override
    public Page<VO> searchPage(T t, Map<String,Object> args,  int page, int size, Sort sort) {
        Page<T> tPage = repository.searchPage(t, new Page<T>(page, size), args, sort);
        Page<VO> voPage = BeanConvertUtil.convertToVoPage(voClass, tPage);
        return voPage;
    }

    @Override
    public Map<Integer, ErrorEnum>  addBatch(List<T> t) {
        Map<Integer, Boolean> longBooleanMap = repository.insertBatch(t);
        Map<Integer, ErrorEnum> resultVOS = new HashMap<>();
        longBooleanMap.forEach( (k,v) -> {
            if(!v){
                resultVOS.put(k, ErrorEnum.CREATE_FAIL);
            }
        });
        return resultVOS;
    }

    @Override
    public Map<Long, ErrorEnum> updateBatch(List<T> t) {
        Map<Long, Boolean> longBooleanMap = repository.updateBatch(t);
        Map<Long, ErrorEnum> resultVOS = new HashMap<>();
        longBooleanMap.forEach( (k,v) -> {
            if(!v){
                resultVOS.put(k, ErrorEnum.UPDATE_FAIL);
            }
        });
        return resultVOS;
    }

    @Override
    public Map<Long,ErrorEnum> deleteBatch(List<Long> ids) {
        Map<Long, Boolean> longBooleanMap = repository.deleteBatch(ids);
        Map<Long, ErrorEnum> resultVOS = new HashMap<>();
        longBooleanMap.forEach( (k,v) -> {
            if(!v){
                resultVOS.put(k, ErrorEnum.DELETE_FAIL);
            }
        });
        return resultVOS;
    }
}
