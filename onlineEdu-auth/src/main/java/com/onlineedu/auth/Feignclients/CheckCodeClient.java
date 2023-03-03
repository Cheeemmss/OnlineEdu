package com.onlineedu.auth.Feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author cheems
 * @Date 2023/3/3 19:13
 */

@FeignClient(value = "checkcode",fallbackFactory = CheckCodeFallbackFactory.class)
public interface CheckCodeClient {

    @PostMapping(value = "/checkcode/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);

}
