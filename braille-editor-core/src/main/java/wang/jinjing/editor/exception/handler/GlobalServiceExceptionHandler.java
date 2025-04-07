package wang.jinjing.editor.exception.handler;

import cn.hutool.core.util.StrUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;

import java.util.Objects;


@ControllerAdvice
public class GlobalServiceExceptionHandler {


    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> ServiceException(ServiceException e){ // 处理方法参数的异常类型
        ErrorEnum errorEnum = e.getErrorEnum();
        if(Objects.isNull(errorEnum)){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }else if(StrUtil.isBlank(e.getMessage())){
            return ResponseEntity.status(errorEnum.getStatus()).body(errorEnum.getMessage());
        }else{
            String detail = errorEnum.getMessage() + " detail: " + e.getMessage();
            return ResponseEntity.status(errorEnum.getStatus()).body(detail);
        }
    }

}
