package wang.jinjing.editor.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.DTO.BaseDTO;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditorTeamDTO extends BaseDTO {

    private Long id;

    private Long ownerId;

    private String teamName;

//    private String uuid;

    private String description;

//    private Long storageQuota;

    private Boolean defaultReadPermission;

    private Boolean defaultWritePermission;

    private Integer maxMembers;

//    private Boolean isActive;

//    private Date createAt;

//    private Date updateAt;
}
