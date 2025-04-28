package wang.jinjing.editor.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@TableName("editor_team")
public class EditorTeam extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("owner")
    private Long ownerId;

    @TableField(exist = false)
    private String ownerNickName;

    @TableField(exist = false)
    private String ownerUsername;

    @TableField("team_name")
    private String teamName;

    @TableField("description")
    private String description;

    @TableField("storage_quota")
    private Long storageQuota;

    @TableField("default_read_permission")
    private Boolean defaultReadPermission;

    @TableField("default_write_permission")
    private Boolean defaultWritePermission;

    @TableField("max_members")
    private Integer maxMembers;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("create_at")
    private Date createAt;

    @TableField("update_at")
    private Date updateAt;
}