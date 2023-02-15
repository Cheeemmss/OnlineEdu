package com.onlineedu.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.dto.BindTeachplanMediaDto;
import com.onlineedu.content.model.dto.SaveTeachPlanDto;
import com.onlineedu.content.model.dto.TeachplanDto;
import com.onlineedu.content.model.entities.Teachplan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author cheems
* @description 针对表【teachplan(课程计划)】的数据库操作Service
* @createDate 2023-01-19 14:15:58
*/

public interface TeachplanService extends IService<Teachplan> {

    List<TeachplanDto> getPlanTreeNodes(Long courseId);

    /**
     * 添加/修改课程计划
     * @param saveTeachPlanDto
     * @return
     */
    Result saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) throws BusinessException;

    Result bindingCourseVideo(BindTeachplanMediaDto bindTeachplanMediaDto) throws BusinessException;
}
