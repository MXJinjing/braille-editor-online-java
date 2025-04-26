package wang.jinjing.editor.pojo.DTO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.DTO.BaseDTO;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OssFileMetadataSearchDTO extends BaseDTO {

    private String realFileName;

    private String path;

    private Date createAtStart;

    private Date createAtEnd;

    private Date lastModifiedAtStart;

    private Date lastModifiedAtEnd;

    private Boolean isDir;

    private String mimeType;

    private Integer page;

    private Integer size;

    private String sorts;
}
