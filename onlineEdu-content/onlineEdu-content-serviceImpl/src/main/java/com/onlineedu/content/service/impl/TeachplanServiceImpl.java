package com.onlineedu.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.mapper.TeachplanMapper;
import com.onlineedu.content.model.dto.SaveTeachPlanDto;
import com.onlineedu.content.model.dto.TeachplanDto;
import com.onlineedu.content.model.entities.Teachplan;
import com.onlineedu.content.service.TeachplanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author cheems
* @description 针对表【teachplan(课程计划)】的数据库操作Service实现
* @createDate 2023-01-19 14:15:58
*/

@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService{

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public Result getPlanTreeNodes(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTeachPlanByCourseId(courseId);
        return Result.success(teachplanDtos);
    }

    @Override
    public Result saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) throws BusinessException {
        Long teachPlanId = saveTeachPlanDto.getId();
        Teachplan teachplan = new Teachplan();
        BeanUtil.copyProperties(saveTeachPlanDto,teachplan);
        if(teachPlanId == null){
            int order = computeOrder(teachplan);
            teachplan.setOrderby(order);
            int insert = teachplanMapper.insert(teachplan);
            if(insert <= 0){
                throw new BusinessException(CommonError.INSERT_EXCEPTION);
            }
            return Result.success("添加课程计划成功");
        }else {
            int i = teachplanMapper.updateById(teachplan);
            if (i <= 0) {
                throw new BusinessException(CommonError.UPDATE_EXCEPTION);
            }
            return Result.success("修改课程计划成功");
        }
    }

    /**
     * 计算与当前课程计划的同级课程计划有几个
     * @param teachplan
     * @return 新添加的课程的order
     */
    private int computeOrder(Teachplan teachplan) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId())
                .eq(Teachplan::getParentid,teachplan.getParentid());
        Integer count = teachplanMapper.selectCount(wrapper);
        return count + 1;
    }
}




