package com.onlineedu.content.service;

import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.entities.CourseCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author cheems
* @description 针对表【course_category(课程分类)】的数据库操作Service
* @createDate 2023-01-20 19:46:32
*/
public interface CourseCategoryService extends IService<CourseCategory> {

    Result getTreeNodes();
}
