package wang.jinjing.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.util.List;
import java.util.Map;

public interface BatchCRUDService<T extends BaseEntity, VO extends BaseVO> {

    Map<Integer, ErrorEnum>  addBatch(List<T> t);

    List<VO> searchList(T t, Map<String,Object> args, Sort sort);

    Page<VO> searchPage(T t, Map<String,Object> args,  int page, int size, Sort sort);

    Map<Long, ErrorEnum> updateBatch(List<T> t);

    Map<Long, ErrorEnum> deleteBatch(List<Long> ids);


}
