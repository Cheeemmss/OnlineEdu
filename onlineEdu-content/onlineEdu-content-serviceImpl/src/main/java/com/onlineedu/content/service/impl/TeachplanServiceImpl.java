package com.onlineedu.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.base.model.Result;
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.content.mapper.TeachplanMapper;
import com.onlineedu.content.mapper.TeachplanMediaMapper;
import com.onlineedu.content.model.dto.BindTeachplanMediaDto;
import com.onlineedu.content.model.dto.SaveTeachPlanDto;
import com.onlineedu.content.model.dto.TeachplanDto;
import com.onlineedu.content.model.entities.Teachplan;
import com.onlineedu.content.model.entities.TeachplanMedia;
import com.onlineedu.content.service.TeachplanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
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

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

    private static final Integer TEACH_PLAN_GRAD_TWO = 2;

    @Override
    public List<TeachplanDto> getPlanTreeNodes(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTeachPlanByCourseId(courseId);
        return teachplanDtos;
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
     * 计算与当前课程计划的同级课程计划有几个 依次来确定新添加课程的order顺序
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

    @Override
    @Transactional
    public Result bindingCourseVideo(BindTeachplanMediaDto bindTeachplanMediaDto) throws BusinessException {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan == null){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade != 2){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"只有二级目录才可以绑定媒资");
        }

        //先删除原来该教学计划绑定的媒资
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));

        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        Long courseId = teachplan.getCourseId();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(new Date());
        int insert = teachplanMediaMapper.insert(teachplanMedia);
        if(insert <= 0){
            throw new BusinessException(CommonError.INSERT_EXCEPTION);
        }
        return Result.success("绑定媒资成功",teachplanMedia);
    }
}




