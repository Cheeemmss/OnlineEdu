package com.onlineedu.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.content.model.dto.TeachplanDto;
import com.onlineedu.content.model.entities.Teachplan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author cheems
* @description 针对表【teachplan(课程计划)】的数据库操作Mapper
* @createDate 2023-01-19 14:15:57
* @Entity com.onlineedu.content.model.entities.Teachplan
*/
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanDto> selectTeachPlanByCourseId(@Param("courseId") Long courseId);
}




