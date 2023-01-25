package com.onlineedu.base.exception;

import com.onlineedu.base.model.SystemCode;

public enum CommonError {


    INSERT_EXCEPTION(SystemCode.CODE_UNKOWN_ERROR,"保存失败"),
    UPDATE_EXCEPTION(SystemCode.CODE_UNKOWN_ERROR,"更新失败"),
    DELETE_EXCEPTION(SystemCode.CODE_UNKOWN_ERROR,"删除失败");

    private String code;
    private String msg;

    CommonError() {
    }

    CommonError(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

