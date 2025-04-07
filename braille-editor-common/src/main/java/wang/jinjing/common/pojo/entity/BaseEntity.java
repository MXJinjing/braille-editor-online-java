package wang.jinjing.common.pojo.entity;

import lombok.Setter;
import wang.jinjing.common.pojo.VO.BaseVO;

@Setter
public class BaseEntity extends BaseVO implements Identifiable<Long> {

    private Long id;

    @Override
    public Long getId() {
        return id;
    }

}
