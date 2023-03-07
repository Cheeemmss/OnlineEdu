package com.onlineedu.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.onlineedu.auth.mapper.XcMenuMapper;
import com.onlineedu.auth.mapper.XcUserMapper;
import com.onlineedu.auth.mapper.XcUserRoleMapper;
import com.onlineedu.auth.model.dto.AuthParamsDto;
import com.onlineedu.auth.model.dto.XcUserExt;
import com.onlineedu.auth.model.po.XcMenu;
import com.onlineedu.auth.model.po.XcUser;
import com.onlineedu.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一的认证接口
 * @Author cheems
 * @Date 2023/3/2 18:38
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private XcMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String authParamsJson) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(authParamsJson, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("无法识别的认证信息格式");
        }

        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "Auth", AuthService.class);
        //具体的认证逻辑execute
        XcUserExt userExt = authService.execute(authParamsDto);
        UserDetails userDetails = getUserPrincipal(userExt);
        return userDetails;
    }

    //获取用户信息
    public UserDetails getUserPrincipal(XcUserExt user){
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        List<XcMenu> permissionList = menuMapper.selectPermissionByUserId(user.getId());
//        log.info("list:{}",permissionList);
        String[] permissions = permissionList.stream().map(XcMenu::getCode).toArray(String[]::new);
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password("").authorities(permissions).build();
        return userDetails;
    }
}
