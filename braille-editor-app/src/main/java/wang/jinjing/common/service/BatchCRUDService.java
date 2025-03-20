package wang.jinjing.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.util.List;
import java.util.Map;

public interface BatchCRUDService<T extends BaseEntity> {

    int addBatch(List<T> t);

    List<T> searchList(T t, Map<String,Object> args, Sort sort);

    Page<T> searchPage(T t, Map<String,Object> args,  int page, int size, Sort sort);

    int updateBatch(List<T> t);

    int deleteBatch(List<Long> ids);


}
