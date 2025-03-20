package wang.jinjing.common.pojo.entity;

import lombok.Getter;
import lombok.Setter;
import wang.jinjing.common.pojo.BasePojo;
import wang.jinjing.common.pojo.VO.BaseVO;

public class BaseEntity extends BaseVO {

    @Getter
    @Setter
    private Long id;
}
