package com.onlineedu.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.content.model.entities.CourseTeacher;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Mapper
* @createDate 2023-01-19 14:15:57
* @Entity com.onlineedu.content.model.entities.CourseTeacher
*/
@Mapper
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {

}




