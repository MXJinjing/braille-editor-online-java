package wang.jinjing.editor.pojo.VO;

import lombok.*;
import wang.jinjing.common.pojo.VO.BaseVO;

import java.time.ZonedDateTime;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3ObjectMetadataVO extends BaseVO {

    private String key;

    private long size;

    private String contentType;

    private ZonedDateTime lastModified;

    private Map<String, String> userMetadata;

}
