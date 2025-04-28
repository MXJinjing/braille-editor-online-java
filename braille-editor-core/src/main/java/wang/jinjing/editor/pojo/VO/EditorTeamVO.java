package wang.jinjing.editor.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.VO.BaseVO;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditorTeamVO extends BaseVO {

    private Long id;

    private Long ownerId;

    // 将userID转变为VO对象
    private EditorUserVO owner;

    private String teamName;

    private String description;

    private Long storageQuota;

    private Boolean defaultReadPermission;

    private Boolean defaultWritePermission;

    private Integer maxMembers;

    private Integer currentMembers;

    private Boolean isActive;

    private Date createAt;

    private Date updateAt;

}
