package wang.jinjing.editor.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("oss_file_recycle")
public class OssFileRecycle extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("origin_file_id")
    private Long originFileId;

    @TableField("origin_file_path")
    private String originFilePath;

    @TableField("operated_by")
    private Long operatedBy;

    @TableField(value = "recycle_at", fill = FieldFill.INSERT)
    private Date recycleAt;

    @TableField("remaining_days")
    private Integer remainingDays;
}