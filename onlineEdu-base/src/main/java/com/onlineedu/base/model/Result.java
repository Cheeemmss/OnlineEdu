package com.onlineedu.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author cheems
 * @Date 2023/1/19 13:08
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    private String code;
    private String msg;
    private Object data;


    public static Result success(String msg){
        return new Result(SystemCode.CODE_SUCCESS,msg,null);
    }

    public static Result success(String msg,Object data){
        return new Result(SystemCode.CODE_SUCCESS,msg,data);
    }

    public static Result success(Object data){
        return new Result(SystemCode.CODE_SUCCESS,null,data);
    }

    public static Result fail(String Code,String msg){
        return new Result(Code,msg,null);
    }

}
