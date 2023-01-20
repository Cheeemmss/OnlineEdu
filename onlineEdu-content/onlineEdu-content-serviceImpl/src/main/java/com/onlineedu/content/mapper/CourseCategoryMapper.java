package com.onlineedu.content.mapper;

import com.onlineedu.content.model.dto.CourseCategoryTreeDto;
import com.onlineedu.content.model.entities.CourseCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author cheems
* @description 针对表【course_category(课程分类)】的数据库操作Mapper
* @createDate 2023-01-20 19:46:32
* @Entity com.onlineedu.content.model.entities.CourseCategory
*/
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectAllTreeNodes();
}




