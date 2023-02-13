package com.onlineedu.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.base.model.SystemStatus;
import com.onlineedu.content.mapper.CourseBaseMapper;
import com.onlineedu.content.mapper.CourseCategoryMapper;
import com.onlineedu.content.mapper.CourseMarketMapper;
import com.onlineedu.content.model.dto.AddCourseDto;
import com.onlineedu.content.model.dto.CourseBaseInfoDto;
import com.onlineedu.content.model.dto.EditCourseDto;
import com.onlineedu.content.model.dto.QueryCourseParamsDto;
import com.onlineedu.content.model.entities.CourseBase;
import com.onlineedu.content.model.entities.CourseCategory;
import com.onlineedu.content.model.entities.CourseMarket;
import com.onlineedu.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author cheems
* @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
* @createDate 2023-01-19 14:15:57
*/
@Service
@Slf4j
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService{


    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public Result pageList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName())
                .eq(StrUtil.isNotBlank(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                .eq(StrUtil.isNotBlank(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus())
                .orderByDesc(CourseBase::getCreateDate);

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> courseBasePage = this.page(page, wrapper);
        return Result.success(courseBasePage);
    }

    /**
     * 创建课程 (创建课程基本信息和营销信息 && 保存数据后回去再次修改的动作)
     * @param companyId
     * @param addCourseDto
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createCourseBase(Long companyId, AddCourseDto addCourseDto) throws Exception {
        //复制Dto中的信息到课程基本信息和课程营销信息两个实体类中
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtil.copyProperties(addCourseDto,courseBase);
        BeanUtil.copyProperties(addCourseDto,courseMarket);

        String charge = courseMarket.getCharge();
        if(charge.equals(SystemStatus.CHARGE_STATUS_NOT_FREE)){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() <= 0){
                throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程价格不可以为空或0");
            }
        }

        //设置机构Id,初始化审核状态,发布状态
        courseBase.setCompanyId(companyId);
        courseBase.setAuditStatus(SystemStatus.AUDIT_STATUS_NOT_COMMIT);
        courseBase.setStatus(SystemStatus.PUBLISH_STATUS_NOT_PUBLISH);
        courseBase.setCreateDate(new Date());

        //1. 插入课程基本信息表
        CourseBase cb = courseBaseMapper.selectById(courseBase.getId());
           //1-1 若已经保存过 又返回来修改 则只需修改不需要插入
        if(cb != null){
            int id = courseBaseMapper.update(courseBase, new UpdateWrapper<CourseBase>().eq("id", cb.getId()));
            int update = courseMarketMapper.update(courseMarket, new UpdateWrapper<CourseMarket>().eq("id", cb.getId()));
            if(id <= 0 || update <=0){
                throw new BusinessException(CommonError.UPDATE_EXCEPTION);
            }
            return Result.success("修改成功",getCourseBaseInfo(courseBase.getId()));
        }
           //1-2 若是第一次插入 还没有保存过该条数据 就直接插入
        int save = courseBaseMapper.insert(courseBase);
        if(save <= 0){
            throw new BusinessException(CommonError.INSERT_EXCEPTION);
        }

        //2. 插入课程营销信息表
        // 插入课程基本信息表后回填了主键 --> 用该主键充当营销表记录的主键
        Long courseId = courseBase.getId();
        courseMarket.setId(courseId);
        int insert = courseMarketMapper.insert(courseMarket);
        if(insert <= 0){
            throw new BusinessException(CommonError.INSERT_EXCEPTION);
        }

       CourseBaseInfoDto courseBaseInfoDto =  getCourseBaseInfo(courseId);
       return Result.success("保存课程基本信息成功",courseBaseInfoDto);
    }


    @Override
    public Result getCourseBaseInfoById(Long courseId) {
        return Result.success(getCourseBaseInfo(courseId));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result editCourseBase(Long companyId,EditCourseDto editCourseDto) throws Exception {

        Long id = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase == null){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程信息不存在");
        }
        if(!companyId.equals(courseBase.getCompanyId())){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"只允许创建课程的机构修改该课程");
        }

        //修改课程基本信息
        CourseBase courseBaseNew = new CourseBase();
        BeanUtil.copyProperties(editCourseDto,courseBaseNew);
        courseBaseNew.setChangeDate(new Date());
        int i = courseBaseMapper.updateById(courseBaseNew);
        if(i <= 0){
            throw new BusinessException(CommonError.UPDATE_EXCEPTION);
        }

        //修改课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtil.copyProperties(editCourseDto,courseMarket);
        String charge = courseMarket.getCharge();
        if(charge.equals(SystemStatus.CHARGE_STATUS_NOT_FREE)){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() <= 0){
                throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程价格不可以为空或0");
            }
        }

        int k = courseMarketMapper.updateById(courseMarket);
        if(k <= 0){
            throw new BusinessException(CommonError.UPDATE_EXCEPTION);
        }

        return Result.success("修改课程信息成功",getCourseBaseInfo(id));
    }

    /**
     * 拼装 CourseBaseInfoDto 获取课程基本信息和营销信息
     * @param courseId
     * @return
     */
    private CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();

        BeanUtil.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtil.copyProperties(courseMarket,courseBaseInfoDto);

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }

}




