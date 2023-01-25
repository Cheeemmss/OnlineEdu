package com.onlineedu.base.model;

/**
 * @Author cheems
 * @Date 2023/1/20 16:50
 * 返回Result的异常Code
 */
public abstract class SystemCode {

    /**
     * 操作成功
     */
    public static final String CODE_SUCCESS = "200";

    /**
     * 参数不合法
     */
    public static final String CODE_PARAMS_ILLEGAL = "201";

    /**
     * 业务异常
     */
    public static final String CODE_UNKOWN_ERROR = "500";

}
