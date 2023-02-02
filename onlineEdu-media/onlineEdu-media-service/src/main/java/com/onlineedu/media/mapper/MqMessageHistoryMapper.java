package com.onlineedu.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.media.model.entities.MqMessageHistory;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【mq_message_history】的数据库操作Mapper
* @createDate 2023-02-01 23:18:27
* @Entity com.onlineedu.media.model.entities.MqMessageHistory
*/
@Mapper
public interface MqMessageHistoryMapper extends BaseMapper<MqMessageHistory> {

}




