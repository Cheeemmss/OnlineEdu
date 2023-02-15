package com.onlineedu.content.api.controller;

import com.onlineedu.content.model.dto.CoursePreviewDto;
import com.onlineedu.content.service.CourseBaseService;
import com.onlineedu.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/2/15 16:01
 */

@Api(value = "课程公开查询接口",tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Resource
    private CourseBaseService courseBaseService;

    @Resource
    private CoursePublishService coursePublishService;

    //课程播放界面 获取课程的基本信息 课程计划信息
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }

}
