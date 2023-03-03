package com.onlineedu.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlineedu.auth.mapper.XcUserMapper;
import com.onlineedu.auth.model.dto.AuthParamsDto;
import com.onlineedu.auth.model.dto.XcUserExt;
import com.onlineedu.auth.model.po.XcUser;
import com.onlineedu.auth.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/3/3 21:37
 */
@Service("wxAuth")
public class WxAuthServiceImpl implements AuthService {

    @Resource
    private XcUserMapper userMapper;

    //认证过程 若认证成功会返回一个xcUser 用于自动登录
    public XcUser WxAuth(String code){
        //根据授权码获取令牌

        //根据令牌获取用户信息

        //保存用户信息

        XcUser xcUser = userMapper.selectById(48);
        return xcUser;
    }

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser user = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);

        return xcUserExt;
    }
}
