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
@TableName("oss_file_metadata")
public class OssFileMetadata extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("file_name")
    private String fileName;

    @TableField("owner_type")
    private String ownerType;

    @TableField("owned_user_id")
    private Long ownedUserId;

    @TableField("owned_group_id")
    private Long ownedGroupId;

    @TableField("name_space")
    private String nameSpace;

    @TableField("virtual_path")
    private String virtualPath;

    @TableField("storage_key")
    private String storageKey;

    @TableField("file_size")
    private Long fileSize;

    @TableField("create_at")
    private Date createAt;

    @TableField("update_at")
    private Date updateAt;

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField("file_hash")
    private String fileHash;

    @TableField("mime_type")
    private String mimeType;
}