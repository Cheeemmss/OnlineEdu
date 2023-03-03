package com.onlineedu.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * 自定义DaoAuthenticationProvider 自定义认证过程
 * @Author cheems
 * @Date 2023/3/3 14:15
 */

@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {

    //这里装配的userDetailsService是我们自定义的UserServiceImpl
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }


    //置空框架原有的检查密码的方法 由我们自己来做(userDetailsService 中的 loadUserByUserName 调取 execute进行判断)
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }
}
