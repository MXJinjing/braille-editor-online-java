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
public class OssRecycleMetadataVO extends BaseVO {

    private String realFileName;

    private String path;

    private Long fileSize;

    private Date createAt;

    private Long createBy;

    private String createByUsername;

    private Date lastModifiedAt;

    private Long lastModifiedBy;

    private String lastModifiedByUsername;

    private String parentPath;

    private Boolean isDir;

    private String fileHash;

    private String mimeType;

    private Long deletedBy;

    private String deletedByUsername;

    private Date deleteAt;

}
