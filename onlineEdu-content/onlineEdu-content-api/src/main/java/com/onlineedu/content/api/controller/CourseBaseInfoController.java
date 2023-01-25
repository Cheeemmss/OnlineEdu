package com.onlineedu.content.api.controller;


import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.dto.AddCourseDto;
import com.onlineedu.content.model.dto.QueryCourseParamsDto;
import com.onlineedu.content.model.entities.CourseBase;
import com.onlineedu.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/1/19 13:12
 */

@Api(tags = "内容管理")
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseService courseBaseService;

    @ApiOperation("课程基本信息分页")
    @PostMapping("/list")
    public Result list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto){
        return courseBaseService.pageList(pageParams,queryCourseParamsDto);
    }

    @ApiOperation("保存课程基本信息/营销信息")
    @PostMapping("/create")
    public Result createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) throws Exception {
         Long id = 12L;
         return courseBaseService.createCourseBase(id,addCourseDto);
    }
}
