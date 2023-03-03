package com.onlineedu.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.onlineedu.auth.Feignclients.CheckCodeClient;
import com.onlineedu.auth.mapper.XcUserMapper;
import com.onlineedu.auth.model.dto.AuthParamsDto;
import com.onlineedu.auth.model.dto.XcUserExt;
import com.onlineedu.auth.model.po.XcUser;
import com.onlineedu.auth.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/3/3 14:24
 */
@Component("passwordAuth")
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //校验验证码
        String checkCode = authParamsDto.getCheckcode();
        String checkCodeKey = authParamsDto.getCheckcodekey();
        if(StringUtils.isBlank(checkCode) || StringUtils.isBlank(checkCodeKey)){
             throw new RuntimeException("验证码为空");
        }
        Boolean verify = checkCodeClient.verify(checkCodeKey, checkCode);
        if(verify == null || !verify){
            throw new RuntimeException("验证码错误");
        }

        //查用户
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, authParamsDto.getUsername()));
        if(xcUser == null){
            throw new RuntimeException("用户不存在");
        }

        String userPasswordDB = xcUser.getPassword();
        boolean matches = passwordEncoder.matches(authParamsDto.getPassword(), userPasswordDB);
        if(!matches){
            throw new RuntimeException("账号密码错误");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }
}
