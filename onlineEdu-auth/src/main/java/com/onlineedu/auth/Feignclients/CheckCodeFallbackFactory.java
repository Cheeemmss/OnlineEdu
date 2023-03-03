package com.onlineedu.auth.Feignclients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @Author cheems
 * @Date 2023/3/3 19:17
 */
@Slf4j
@Component
public class CheckCodeFallbackFactory implements FallbackFactory<CheckCodeClient> {

    @Override
    public CheckCodeClient create(Throwable cause) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.info("验证码服务异常:{}",cause.getMessage());
                return null;
            }
        };
    }
}
