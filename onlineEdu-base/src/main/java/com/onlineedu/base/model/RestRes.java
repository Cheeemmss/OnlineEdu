package com.onlineedu.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @description 通用结果类型
 * @author Mr.M
 * @date 2022/9/13 14:44
 * @version 1.0
 */

 @Data
 @ToString
public class RestRes<T> {

  /**
   * 响应编码,0为正常,-1错误
   */
  private int code;

  /**
   * 响应提示信息
   */
  private String msg;

  /**
   * 响应内容
   */
  private T result;


  public RestRes() {
   this(0, "success");
  }

  public RestRes(int code, String msg) {
   this.code = code;
   this.msg = msg;
  }

  /**
   * 错误信息的封装
   *
   * @param msg
   * @param <T>
   * @return
   */
  public static <T> RestRes<T> validfail(String msg) {
   RestRes<T> response = new RestRes<T>();
   response.setCode(-1);
   response.setMsg(msg);
   return response;
  }
  public static <T> RestRes<T> validfail(T result, String msg) {
   RestRes<T> response = new RestRes<T>();
   response.setCode(-1);
   response.setResult(result);
   response.setMsg(msg);
   return response;
  }



  /**
   * 添加正常响应数据（包含响应内容）
   *
   * @return RestResponse Rest服务封装相应数据
   */
  public static <T> RestRes<T> success(T result) {
   RestRes<T> response = new RestRes<T>();
   response.setResult(result);
   return response;
  }
  public static <T> RestRes<T> success(T result, String msg) {
   RestRes<T> response = new RestRes<T>();
   response.setResult(result);
   response.setMsg(msg);
   return response;
  }

  /**
   * 添加正常响应数据（不包含响应内容）
   *
   * @return RestResponse Rest服务封装相应数据
   */
  public static <T> RestRes<T> success() {
   return new RestRes<T>();
  }


  public Boolean isSuccessful() {
   return this.code == 0;
  }

 }