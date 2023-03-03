package com.onlineedu.content.api.controller;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.dto.CoursePreviewDto;
import com.onlineedu.content.service.CoursePublishService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/2/15 14:09
 */

@Controller
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;

//    @ApiOperation("课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

//    @ApiOperation("课程提交审核")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public Result commitAudit(@PathVariable("courseId") Long courseId) throws BusinessException {
        Long companyId = 12L;
        coursePublishService.commitAudit(companyId,courseId);
        return Result.success("提交审核成功");
    }

//    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public Result coursepublish(@PathVariable("courseId") Long courseId) throws BusinessException {
        Long companyId = 12L;
        coursePublishService.publish(companyId,courseId);
        return Result.success("课程发布中,请稍后查看");
    }

}
