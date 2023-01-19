package com.onlineedu.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.content.model.entities.CourseBase;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【course_base(课程基本信息)】的数据库操作Mapper
* @createDate 2023-01-19 14:15:57
* @Entity com.onlineedu.content.model.entities.CourseBase
*/
@Mapper
public interface CourseBaseMapper extends BaseMapper<CourseBase> {

}




