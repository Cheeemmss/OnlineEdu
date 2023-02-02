package com.onlineedu.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.media.model.entities.MqMessage;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【mq_message】的数据库操作Mapper
* @createDate 2023-02-01 23:18:27
* @Entity com.onlineedu.media.model.entities.MqMessage
*/
@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {

}




