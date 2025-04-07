package wang.jinjing.common.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVO <T extends BaseVO> extends BaseVO{

    private List<T> records;

    private long total;

    private long size;

    private long current;

}
