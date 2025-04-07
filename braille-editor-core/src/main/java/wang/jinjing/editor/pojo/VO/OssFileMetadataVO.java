package wang.jinjing.editor.pojo.VO;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class OssFileMetadataVO extends BaseVO {@TableId(value = "id", type = IdType.AUTO)
private Long id;

    private String realFileName;

    private String s3Bucket;

    private String s3Key;

    private Long fileSize;

    private Date createAt;

    private Long createBy;

    private String createByUsername;

    private Date updateAt;

    private Long lastUpdateBy;

    private String lastUpdateByUsername;

    private Long parentId;

    private String parentPath;

    private Boolean isDir;

    private String fileHash;

    private String mimeType;

}
