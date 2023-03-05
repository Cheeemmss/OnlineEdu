package com.onlineedu.auth.controller;

import com.alibaba.fastjson.JSON;
import com.onlineedu.auth.model.po.XcUser;
import com.onlineedu.auth.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/3/3 20:35
 */
@Slf4j
@Controller
public class WxLoginController {

    @Resource
    private WxAuthServiceImpl wxAuthService;

    //用户确认授权后的回调地址
    @GetMapping("/api/ucenter/wx/callback")
    public String wxLogin(String code, String state){
        XcUser xcUser = wxAuthService.WxAuth(code);
        if(xcUser == null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser.getUsername();
        //重定向的该路径中若包含了username并且认证方式为wx的话 会自动调用认证方法进行直接登录
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";
    }
}
