package com.onlineedu.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.content.mapper.CourseBaseMapper;
import com.onlineedu.content.mapper.CoursePublishMapper;
import com.onlineedu.content.model.dto.CourseBaseInfoDto;
import com.onlineedu.content.model.dto.CoursePreviewDto;
import com.onlineedu.content.model.dto.TeachplanDto;
import com.onlineedu.content.model.entities.CourseBase;
import com.onlineedu.content.model.entities.CoursePublish;
import com.onlineedu.content.model.entities.Teachplan;
import com.onlineedu.content.service.CourseBaseService;
import com.onlineedu.content.service.CoursePublishService;
import com.onlineedu.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author cheems
* @description 针对表【course_publish(课程发布)】的数据库操作Service实现
* @createDate 2023-01-19 14:15:57
*/
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish>
    implements CoursePublishService{

    @Resource
    private CourseBaseService courseBaseService;

    @Resource
    TeachplanService teachplanService;

    @Override
    public CoursePreviewDto

    getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = (CourseBaseInfoDto) courseBaseService.getCourseBaseInfoById(courseId).getData();
        List<TeachplanDto> planTreeNodes = teachplanService.getPlanTreeNodes(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(planTreeNodes);
        return coursePreviewDto;
    }
}




