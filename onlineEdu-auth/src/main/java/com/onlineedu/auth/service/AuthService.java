package com.onlineedu.auth.service;

import com.onlineedu.auth.model.dto.AuthParamsDto;
import com.onlineedu.auth.model.dto.XcUserExt;

/**
 * 认证服务 对于不同的认证方式有着不同的实现
 * @Author cheems
 * @Date 2023/3/3 14:22
 */
public interface AuthService {

    /**
     * 具体的认证方法
     * @param authParamsDto 用户信息
     * @return
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
