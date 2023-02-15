package com.onlineedu.content.api.controller;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.dto.BindTeachplanMediaDto;
import com.onlineedu.content.model.dto.SaveTeachPlanDto;
import com.onlineedu.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/1/26 17:50
 */

@Api(tags = "课程计划")
@RestController
@RequestMapping("/TeachPlan")
public class TeachplanController {

    @Resource
    private TeachplanService teachplanService;

    @ApiOperation("获取课程计划树")
    @GetMapping("/treeNodes/{courseId}")
    public Result getTreeNodes(@PathVariable Long courseId){
           return Result.success(teachplanService.getPlanTreeNodes(courseId));
    }

    @ApiOperation("添加/修改课程计划")
    @PostMapping("/saveTeachPlan")
    public Result saveTeachPlan(@RequestBody SaveTeachPlanDto saveTeachPlanDto) throws BusinessException {
           return teachplanService.saveTeachPlan(saveTeachPlanDto);
    }

    @ApiOperation("绑定课程对应的视频")
    @PostMapping("/binding")
    public Result bindingCourseVideo(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) throws BusinessException {
        return teachplanService.bindingCourseVideo(bindTeachplanMediaDto);
    }
}
