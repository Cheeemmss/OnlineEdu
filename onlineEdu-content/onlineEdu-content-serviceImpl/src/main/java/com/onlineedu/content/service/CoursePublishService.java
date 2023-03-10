package com.onlineedu.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.content.model.dto.CoursePreviewDto;
import com.onlineedu.content.model.entities.CoursePublish;
import org.apache.ibatis.annotations.Mapper;

import java.io.File;

/**
* @author cheems
* @description 针对表【course_publish(课程发布)】的数据库操作Service
* @createDate 2023-01-19 14:15:57
*/

public interface CoursePublishService extends IService<CoursePublish> {

     CoursePreviewDto getCoursePreviewInfo(Long courseId);

     void commitAudit(Long companyId,Long courseId) throws BusinessException;

     public void publish(Long companyId,Long courseId) throws BusinessException;

     public File generateCourseHtml(Long courseId);

     public void  uploadCourseHtml(Long courseId, File file) throws BusinessException;

    CoursePublish getCoursePublish(Long courseId);
}
