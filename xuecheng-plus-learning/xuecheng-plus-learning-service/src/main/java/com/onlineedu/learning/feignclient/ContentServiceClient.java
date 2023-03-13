package com.onlineedu.learning.feignclient;

import com.onlineedu.content.model.entities.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @description 内容管理服务远程接口
 * @author Mr.M
 * @date 2022/9/20 20:29
 * @version 1.0
 */
@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

      @GetMapping ( "/content/r/coursepublish/{courseId}")
      public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);

 }
