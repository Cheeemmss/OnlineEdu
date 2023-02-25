package com.onlineedu.content.service.handler;


import com.onlineedu.messagesdk.model.po.MqMessage;
import com.onlineedu.messagesdk.service.MessageProcessAbstract;
import com.onlineedu.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author cheems
 * @Date 2023/2/25 15:19
 */

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Resource
    private MqMessageService mqMessageService;


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
    public boolean execute(MqMessage mqMessage) {
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


    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.info("保存课程索引到es");
    }

    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.info("添加课程缓存至redis");
    }

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.info("生成静态页面上传至Minio");
    }
}
