package wang.jinjing.common.pojo.VO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import wang.jinjing.common.pojo.ErrorEnum;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationResultVO extends BaseVO{

    long resourceId;

    int code;  //Restful HTTP Status Code

    String message;

    // 忽略NULL
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Object data;

    public static OperationResultVO success(long id){
        return new OperationResultVO(id, 200, "Success",null);
    }

    public static OperationResultVO success(long id, Object data){
        return new OperationResultVO(id, 200, "Success",data);
    }

    public static OperationResultVO fail(long id, int code, String message){
        return new OperationResultVO(id, code, message,null);
    }

    public static OperationResultVO fail(long id, String message){
        return new OperationResultVO(id, 500, message,null);
    }

    public static OperationResultVO fail(long id){
        return new OperationResultVO(id, 500, "Fail",null);
    }
}
