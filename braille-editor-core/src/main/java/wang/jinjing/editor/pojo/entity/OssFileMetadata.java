package wang.jinjing.editor.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("oss_file_metadata")
public class OssFileMetadata extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("real_file_name")
    private String realFileName;

    @TableField("s3_bucket")
    private String s3Bucket;

    @TableField("s3_key")
    private String s3Key;

    @TableField("file_size")
    private Long fileSize;

    @TableField(value = "create_at", fill = FieldFill.INSERT)
    private Date createAt;

    @TableField("create_by")
    private Long createBy;

    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private Date updateAt;

    @TableField("last_update_by")
    private Long lastUpdateBy;

    @TableField("parent_id")
    private Long parentId;

    @TableField("parent_path")
    private String parentPath;

    @TableField("is_dir")
    private Boolean isDir;

    @TableField("file_hash")
    private String fileHash;

    @TableField("mime_type")
    private String mimeType;

}