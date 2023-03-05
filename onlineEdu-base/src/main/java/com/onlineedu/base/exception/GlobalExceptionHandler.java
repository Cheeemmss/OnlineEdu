package com.onlineedu.base.exception;

import com.onlineedu.base.model.Result;
import com.onlineedu.base.model.SystemCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static com.onlineedu.base.model.SystemCode.CODE_NO_PERMISSION;

/**
 * @Author cheems
 * @Date 2023/1/25 15:03
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result BusinessExceptionHandler(BusinessException e){
        e.printStackTrace();
        return Result.fail(e.getCode(),e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Result UnkownExceptionHandler(Exception e){
        e.printStackTrace();
        if(e.getMessage().equals("不允许访问")){
            //这里不知道为什么单独定义一个handler捕获不到AccessDeniedException 依然走的这个UnkownExceptionHandler
            return Result.fail(CODE_NO_PERMISSION,"您无此操作权限");
        }
        return Result.fail(SystemCode.CODE_UNKOWN_ERROR,"未知系统异常");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result paramsValidExceptionHandler(MethodArgumentNotValidException argumentNotValidException) {

        BindingResult bindingResult = argumentNotValidException.getBindingResult();
        StringBuffer errMsg = new StringBuffer();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> {
            errMsg.append(error.getDefaultMessage()).append(" ");
        });

        return  Result.fail(SystemCode.CODE_PARAMS_ILLEGAL,errMsg.toString());
    }

}
