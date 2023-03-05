package com.onlineedu.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlineedu.auth.mapper.XcUserMapper;
import com.onlineedu.auth.mapper.XcUserRoleMapper;
import com.onlineedu.auth.model.dto.AuthParamsDto;
import com.onlineedu.auth.model.dto.XcUserExt;
import com.onlineedu.auth.model.po.XcUser;
import com.onlineedu.auth.model.po.XcUserRole;
import com.onlineedu.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @Author cheems
 * @Date 2023/3/3 21:37
 */
@Slf4j
@Service("wxAuth")
public class WxAuthServiceImpl implements AuthService {

    @Value("${wx.open.app_id}")
    private String appOpenId;

    @Value("${wx.open.app_secret}")
    private String appSecretId;

    @Resource
    private XcUserMapper userMapper;

    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ApplicationContext applicationContext;


    //认证过程 若认证成功会返回一个xcUser 用于自动登录
    public XcUser WxAuth(String code){
        //根据授权码获取令牌
        Map<String,String> accessTokenMap = getAccessToken(code);
        //根据令牌获取用户信息
        String accessToken = accessTokenMap.get("access_token");
        String openId = accessTokenMap.get("openid");
        Map<String,String> userInfoMap = getUserInfoByToken(accessToken,openId);
        //保存用户信息
        WxAuthServiceImpl wxAuthServiceProxy = applicationContext.getBean(WxAuthServiceImpl.class);
        XcUser xcUser = wxAuthServiceProxy.saveUser(userInfoMap);
        return xcUser;
    }


    /**
     * 根据微信重定向提供的授权码获取访问令牌
     * @param code 授权码
     * @return
     */
    private Map<String, String> getAccessToken(String code) {
        String urlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(urlTemplate, appOpenId, appSecretId, code);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String body = responseEntity.getBody();
        Map map = JSON.parseObject(body, Map.class);
        return map;
    }

    /**
     * 根据获取的访问令牌获取用户信息
     * @param accessToken 访问令牌
     * @param openId 普通用户的标识，对当前开发者帐号唯一
     * @return
     */
    private Map<String, String> getUserInfoByToken(String accessToken,String openId) {
        String urlTemplate =  "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(urlTemplate, accessToken, openId);
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        String body = entity.getBody();
        Map map = JSON.parseObject(body, Map.class);
        return map;
    }

    /**
     * 保存微信用户的数据到数据库
     * @param userInfoMap
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public XcUser saveUser(Map<String, String> userInfoMap) {
        String unionId = userInfoMap.get("unionid");
        XcUser xcUser = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionId));
        //该用户已经使用微信登录方式登录过该系统
        if(xcUser != null){
            return xcUser;
        }
        String userId = UUID.randomUUID().toString();
        xcUser = new XcUser();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionId);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfoMap.get("nickname"));
        xcUser.setUserpic(userInfoMap.get("headimgurl"));
        xcUser.setName(userInfoMap.get("nickname"));
        xcUser.setUsername(unionId); //unionId 作为username
        xcUser.setPassword(unionId);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        userMapper.insert(xcUser);
        //设置默认角色
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;

    }

    /**
     * 正常情况下 微信扫码登录时一定可以登录成功的
     * @param authParamsDto 用户信息
     * @return
     */
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
