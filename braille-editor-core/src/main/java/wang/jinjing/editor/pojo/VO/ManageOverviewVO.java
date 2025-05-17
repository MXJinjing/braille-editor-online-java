package wang.jinjing.editor.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.VO.BaseVO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManageOverviewVO extends BaseVO {

    private Long userCount;

    private Long visitCount;

    private Long fileCount;

    private Long fileTodayCount;

}
