package com.xuecheng.learning.service;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.content.model.entities.CoursePublish;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description 我的课程表service接口
 * @author Mr.M
 * @date 2022/10/2 16:07
 * @version 1.0
 */
public interface MyCourseTablesService {

    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) throws BusinessException;

    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish) throws BusinessException;

    public XcChooseCourse addChargeCourse(String userId,CoursePublish coursepublish);

    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse) throws BusinessException;

    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

}
