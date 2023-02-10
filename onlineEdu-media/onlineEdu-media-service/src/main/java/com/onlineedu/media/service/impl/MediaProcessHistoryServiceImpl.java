package com.onlineedu.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.media.mapper.MediaFilesMapper;
import com.onlineedu.media.mapper.MediaProcessHistoryMapper;
import com.onlineedu.media.mapper.MediaProcessMapper;
import com.onlineedu.media.model.entities.MediaFiles;
import com.onlineedu.media.model.entities.MediaProcess;
import com.onlineedu.media.model.entities.MediaProcessHistory;
import com.onlineedu.media.service.MediaProcessHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.onlineedu.base.model.SystemStatus.MEDIA_PROCESS_PROCESS_FAIL;
import static com.onlineedu.base.model.SystemStatus.MEDIA_PROCESS_PROCESS_SUCCESS;

/**
* @author cheems
* @description 针对表【media_process_history】的数据库操作Service实现
* @createDate 2023-02-01 23:18:27
*/
@Service
@Slf4j
public class MediaProcessHistoryServiceImpl extends ServiceImpl<MediaProcessHistoryMapper, MediaProcessHistory>
    implements MediaProcessHistoryService{

}




