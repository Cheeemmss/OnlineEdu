package com.onlineedu.content.api.controller;

import com.onlineedu.base.model.Result;
import com.onlineedu.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/1/20 19:48
 */
@Api(tags = "课程分类")
@RestController
@RequestMapping("/courseCategory")
public class CourseCategoryController {

    @Resource
    private CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类树状节点")
    @GetMapping("/treeNodes")
    public Result getTreeNodes(){
          return courseCategoryService.getTreeNodes();
    }
}
