package com.onlineedu.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.dto.QueryCourseParamsDto;
import com.onlineedu.content.model.entities.CourseBase;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【course_base(课程基本信息)】的数据库操作Service
* @createDate 2023-01-19 14:15:57
*/

public interface CourseBaseService extends IService<CourseBase> {

    Result pageList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
