package com.onlineedu.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.content.model.entities.CourseMarket;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【course_market(课程营销信息)】的数据库操作Mapper
* @createDate 2023-01-19 14:15:57
* @Entity com.onlineedu.content.model.entities.CourseMarket
*/
@Mapper
public interface CourseMarketMapper extends BaseMapper<CourseMarket> {

}




