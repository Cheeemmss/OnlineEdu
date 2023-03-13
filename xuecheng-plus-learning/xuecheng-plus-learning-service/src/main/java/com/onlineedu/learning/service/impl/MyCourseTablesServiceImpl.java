package com.onlineedu.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.content.model.entities.CoursePublish;
import com.onlineedu.learning.feignclient.ContentServiceClient;
import com.onlineedu.learning.mapper.XcChooseCourseMapper;
import com.onlineedu.learning.mapper.XcCourseTablesMapper;
import com.onlineedu.learning.service.MyCourseTablesService;
import com.onlineedu.learning.model.dto.XcChooseCourseDto;
import com.onlineedu.learning.model.dto.XcCourseTablesDto;
import com.onlineedu.learning.model.po.XcChooseCourse;
import com.onlineedu.learning.model.po.XcCourseTables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.onlineedu.base.model.SystemCode.CODE_UNKOWN_ERROR;


@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Resource
    private ContentServiceClient contentServiceClient;

    @Resource
    private XcChooseCourseMapper chooseCourseMapper;

    @Resource
    private XcCourseTablesMapper xcCourseTablesMapper;

    @Resource
    private ApplicationContext applicationContext;


    /**
     * 添加选课
     * @param userId
     * @param courseId
     * @return 学习资格
     * @throws BusinessException
     */
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) throws BusinessException {
//        log.info(userId);
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"课程不存在");
        }
        String charge = coursepublish.getCharge();
        MyCourseTablesService myCourseTablesServiceProxy = applicationContext.getBean(MyCourseTablesService.class);
        XcChooseCourse xcChooseCourse = null;
        if("201000".equals(charge)){
            //免费课程 添加选课记录表 + 我的课程表
             xcChooseCourse = myCourseTablesServiceProxy.addFreeCourse(userId, coursepublish);
        }else {
            //收费课程 只添加选课记录表
             xcChooseCourse = myCourseTablesServiceProxy.addChargeCourse(userId, coursepublish);
        }

        //获取学习资格并返回
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        return xcChooseCourseDto;
    }

    /**
     * 添加免费课程 免费课程加入选课记录表 + 我的课程表
     * @param userId
     * @param coursepublish
     * @return
     * @throws BusinessException
     */
    @Transactional(rollbackFor = Exception.class)
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish) throws BusinessException {
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001") //免费课程
                .eq(XcChooseCourse::getStatus, "701001");  //选课成功

        //看课程是否已添加过这门课若添加过直接返回
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses != null && xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }
        //1. 没有则向选课记录表添加一条记录
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setCoursePrice(0f);
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功
        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        chooseCourseMapper.insert(xcChooseCourse);

        //2. 向我的课程表添加一条记录
        addCourseTables(xcChooseCourse);

        return xcChooseCourse;
    }

    /**
     * 添加收费课程 收费课程只添加到选课表
     * @param userId
     * @param coursepublish
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public XcChooseCourse addChargeCourse(String userId,CoursePublish coursepublish){
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002") //收费课程
                .eq(XcChooseCourse::getStatus, "701002");  //待支付

        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses != null && xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice().floatValue());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        chooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

    /**
     * 添加到我的课程表
     * @param xcChooseCourse 选课记录
     * @return
     * @throws BusinessException
     */
    @Transactional(rollbackFor = Exception.class)
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse) throws BusinessException {
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            throw new BusinessException(CODE_UNKOWN_ERROR,"选课未成功，无法添加到课程表");
        }
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables!=null){
            return xcCourseTables;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;

    }


    /**
     * 查询某人课程表中的某个课程的信息
     * @param userId
     * @param courseId
     * @return
     */
    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        LambdaQueryWrapper<XcCourseTables> wrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId);
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(wrapper);
        return xcCourseTables;
    }


    /**
     * 判断用户是否有某门课程的学习资格
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if(xcCourseTables == null){
            //课程表里面都没有 肯定不可以学
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        LocalDateTime validTimeEnd = xcCourseTables.getValidtimeEnd();
        if(LocalDateTime.now().isAfter(validTimeEnd)){
            //课程表里存在但是已过期
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        //正常学习
        xcCourseTablesDto.setLearnStatus("702001");
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        return xcCourseTablesDto;
    }

    @Override
    @Transactional
    public void saveChooseCourseSuccess(String courseChooseId) throws BusinessException {
        XcChooseCourse xcChooseCourse = chooseCourseMapper.selectById(courseChooseId);
        if(xcChooseCourse == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"不存的该选课记录" + courseChooseId);
        }
        String status = xcChooseCourse.getStatus();
        if("701001".equals(status)){
            throw new BusinessException(CODE_UNKOWN_ERROR,"课程已选课过" + courseChooseId);
        }
        xcChooseCourse.setStatus("701001");
        int update = chooseCourseMapper.updateById(xcChooseCourse);
        if(update <= 0){
            throw new BusinessException(CODE_UNKOWN_ERROR,"保存选课信息状态失败" + courseChooseId);
        }
        addCourseTables(xcChooseCourse);
    }
}
