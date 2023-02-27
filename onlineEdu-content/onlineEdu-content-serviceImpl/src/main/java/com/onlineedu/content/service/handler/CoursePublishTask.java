package com.onlineedu.content.service.handler;


import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.content.service.CoursePublishService;
import com.onlineedu.messagesdk.model.po.MqMessage;
import com.onlineedu.messagesdk.service.MessageProcessAbstract;
import com.onlineedu.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @Author cheems
 * @Date 2023/2/25 15:19
 */

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private CoursePublishService coursePublishService;

    //课程发布消息类型
    public static final String MESSAGE_TYPE = "course_publish";


    @XxlJob("coursePublishHandler")
    public void coursePublishHandler(){
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("shardIndex="+shardIndex+",shardTotal="+shardTotal);

        //分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,MESSAGE_TYPE,5,60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) throws BusinessException {
        //MqMessage 每条待处理的消息
            String businessKey1 = mqMessage.getBusinessKey1();
            long courseId = Integer.parseInt(businessKey1);
            //课程静态化

            generateCourseHtml(mqMessage,courseId);
            //课程缓存
            saveCourseCache(mqMessage,courseId);
            //课程索引
            saveCourseIndex(mqMessage,courseId);
            return true;
    }


    private void generateCourseHtml(MqMessage mqMessage, long courseId) throws BusinessException {
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();

        //消息幂等性处理 第一阶段(上传静态页面干过了就不用再干了)
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne>0){
            log.debug("当前阶段是静态化课程信息任务已经完成不再处理,任务信息:{}",mqMessage);
            return ;
        }

        //生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
           throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程静态化异常");
        }
        //上传静态化页面
        coursePublishService.uploadCourseHtml(courseId,file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(id);
    }



    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.info("添加课程缓存至redis");
    }

    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.info("保存课程索引到es");
    }

}
