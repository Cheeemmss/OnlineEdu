package com.onlineedu.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.media.model.entities.MediaProcess;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheems
* @description 针对表【media_process】的数据库操作Mapper
* @createDate 2023-02-01 23:18:26
* @Entity com.onlineedu.media.model.entities.MediaProcess
*/
@Mapper
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

}




