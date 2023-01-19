package com.onlineedu.content.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.mapper.CourseBaseMapper;
import com.onlineedu.content.model.dto.QueryCourseParamsDto;
import com.onlineedu.content.model.entities.CourseBase;
import com.onlineedu.content.service.CourseBaseService;
import org.springframework.stereotype.Service;

/**
* @author cheems
* @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
* @createDate 2023-01-19 14:15:57
*/
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService{

    @Override
    public Result pageList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName())
                .eq(StrUtil.isNotBlank(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                .eq(StrUtil.isNotBlank(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> courseBasePage = this.page(page, wrapper);
        return Result.success(courseBasePage);
    }
}




