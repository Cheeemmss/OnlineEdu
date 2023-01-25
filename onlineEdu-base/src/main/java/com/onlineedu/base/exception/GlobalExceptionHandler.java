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

import java.util.List;

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
