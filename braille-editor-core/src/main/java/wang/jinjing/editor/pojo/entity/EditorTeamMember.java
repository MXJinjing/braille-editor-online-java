package wang.jinjing.editor.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.entity.BaseEntity;
import wang.jinjing.editor.pojo.enums.TeamRoleEnum;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("editor_team_member")
public class EditorTeamMember extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("team_id")
    private Long teamId;

    @TableField("user_id")
    private Long userId;

    @TableField("team_role")
    private TeamRoleEnum teamRole;

    @TableField("join_at")
    private Date joinAt;
}