package com.onlineedu.media.model.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName mq_message
 */
@TableName(value ="mq_message")
@Data
public class MqMessage implements Serializable {
    /**
     * 消息id
     */
    @TableId
    private String id;

    /**
     * 消息类型代码
     */
    private String messageType;

    /**
     * 关联业务信息
     */
    private String businessKey1;

    /**
     * 关联业务信息
     */
    private String businessKey2;

    /**
     * 关联业务信息
     */
    private String businessKey3;

    /**
     * 消息队列主机
     */
    private String mqHost;

    /**
     * 消息队列端口
     */
    private Integer mqPort;

    /**
     * 消息队列虚拟主机
     */
    private String mqVirtualhost;

    /**
     * 队列名称
     */
    private String mqQueue;

    /**
     * 通知次数
     */
    private Object informNum;

    /**
     * 处理状态，0:初始，1:成功
     */
    private String state;

    /**
     * 回复失败时间
     */
    private Date returnfailureDate;

    /**
     * 回复成功时间
     */
    private Date returnsuccessDate;

    /**
     * 回复失败内容
     */
    private String returnfailureMsg;

    /**
     * 最近通知时间
     */
    private Date informDate;

    /**
     * 阶段1处理状态, 0:初始，1:成功
     */
    private String stageState1;

    /**
     * 阶段2处理状态, 0:初始，1:成功
     */
    private String stageState2;

    /**
     * 阶段3处理状态, 0:初始，1:成功
     */
    private String stageState3;

    /**
     * 阶段4处理状态, 0:初始，1:成功
     */
    private String stageState4;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}