package wang.jinjing.common.service;

import wang.jinjing.common.pojo.entity.BaseEntity;

public interface BasicCRUDService<T extends BaseEntity> {

    int addOne(T t);

    T findById(Long id);

    int updateOne(Long id, T t);

    int deleteOne(Long id);

}
