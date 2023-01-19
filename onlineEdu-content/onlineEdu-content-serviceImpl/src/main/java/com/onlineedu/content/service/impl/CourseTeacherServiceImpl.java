package com.onlineedu.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.content.mapper.CourseTeacherMapper;
import com.onlineedu.content.model.entities.CourseTeacher;
import com.onlineedu.content.service.CourseTeacherService;
import org.springframework.stereotype.Service;

/**
* @author cheems
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service实现
* @createDate 2023-01-19 14:15:57
*/
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher>
    implements CourseTeacherService{

}




