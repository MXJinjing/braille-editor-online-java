package wang.jinjing.editor.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.DTO.BaseDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadBytesDTO extends BaseDTO {

    private String filename;

    private byte[] content;

    private String path;

    private String mimeType;
}
