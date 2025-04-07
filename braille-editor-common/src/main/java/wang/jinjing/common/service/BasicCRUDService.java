package wang.jinjing.common.service;

import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.entity.BaseEntity;

public interface BasicCRUDService<T extends BaseEntity, VO extends BaseVO> {

    long addOne(T t);

    VO findById(Long id);

    int updateOne(Long id, T t);

    int deleteOne(Long id);

}
