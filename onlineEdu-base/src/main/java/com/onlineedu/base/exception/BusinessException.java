package com.onlineedu.base.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author cheems
 * @Date 2023/1/25 15:00
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends Exception{

    private String code;
    private String msg;

    public BusinessException() {
    }

    public BusinessException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(CommonError commonError) {
        super(commonError.getMsg());
        this.code = commonError.getCode();
        this.msg = commonError.getMsg();
    }

}
