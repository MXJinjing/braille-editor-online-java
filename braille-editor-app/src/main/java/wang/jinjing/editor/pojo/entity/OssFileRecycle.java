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

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@TableName("oss_file_recycle")
public class OssFileRecycle extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("file_id")
    private Long fileId;

    @TableField("operated_by")
    private Long operatedBy;

    @TableField("recycle_at")
    private Date recycleAt;

    @TableField("remaining_days")
    private Integer remainingDays;
}