package com.onlineedu.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.media.model.entities.MediaProcess;

import java.util.List;

/**
* @author cheems
* @description 针对表【media_process】的数据库操作Service
* @createDate 2023-02-01 23:18:26
*/
public interface MediaProcessService extends IService<MediaProcess> {

    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int num);

    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
