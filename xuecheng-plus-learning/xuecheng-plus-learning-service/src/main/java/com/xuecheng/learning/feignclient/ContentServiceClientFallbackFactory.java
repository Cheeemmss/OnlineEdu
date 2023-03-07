package com.xuecheng.learning.feignclient;

import com.onlineedu.content.model.entities.CoursePublish;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/3 8:03
 */
@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public CoursePublish getCoursepublish(Long courseId) {
                log.error("远程调用内容管理服务熔断异常：{}",throwable.getMessage());
                return new CoursePublish();
            }
        };
    }
}