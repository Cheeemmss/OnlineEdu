package com.xuecheng.learning.api;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.SystemCode;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.service.impl.MyCourseTablesServiceImpl;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.onlineedu.base.model.SystemCode.CODE_UNKOWN_ERROR;

/**
 * @author cheems
 * @Date 2023-3-7
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Resource
    private MyCourseTablesServiceImpl courseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) throws BusinessException {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"请登录后继续选课");
        }
        String userId = user.getId();
        XcChooseCourseDto xcChooseCourseDto = courseTablesService.addChooseCourse(userId, courseId);
        return xcChooseCourseDto;
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) throws BusinessException {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"请登录后继续选课");
        }
        String userId = user.getId();
        return  courseTablesService.getLearningStatus(userId, courseId);

    }


}
