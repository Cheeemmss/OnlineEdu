package com.onlineedu.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author cheems
 * @Date 2023/2/28 19:01
 */

@EnableFeignClients(basePackages = {"com.onlineedu.auth.Feignclients"})
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}

