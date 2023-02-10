package com.onlineedu.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.media.mapper.MediaFilesMapper;
import com.onlineedu.media.mapper.MediaProcessHistoryMapper;
import com.onlineedu.media.mapper.MediaProcessMapper;
import com.onlineedu.media.model.entities.MediaFiles;
import com.onlineedu.media.model.entities.MediaProcess;
import com.onlineedu.media.model.entities.MediaProcessHistory;
import com.onlineedu.media.service.MediaProcessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.onlineedu.base.model.SystemStatus.MEDIA_PROCESS_PROCESS_FAIL;
import static com.onlineedu.base.model.SystemStatus.MEDIA_PROCESS_PROCESS_SUCCESS;

/**
* @author cheems
* @description 针对表【media_process】的数据库操作Service实现
* @createDate 2023-02-01 23:18:26
*/
@Service
public class MediaProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess>
    implements MediaProcessService{


    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 根据 xxl-job 分片广播每个handler分发的shardIndex 来查询该执行器所应该执行的任务(保证不同执行器不会执行相同的任务)
     * 当前handler要处理的任务的Id满足 => Id % shardTotal = shardIndex  (取值为 0 -- shardTotal-1)
     * @param shardTotal 总分片数
     * @param shardIndex 当前分片索引
     * @param num 一次查多少个 最大不可以超过cpu核心数
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int num) {
        List<MediaProcess> mediaProcessList = mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, num);
        return mediaProcessList;
    }

    /**
     * 保存处理任务的信息到数据库
     * @param taskId 任务Id
     * @param status 任务处理状态
     * @param fileId 处理的视频Id
     * @param url 处理视频生成的url
     * @param errorMsg 处理失败的错误信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return;
        }
        //任务失败
        if(MEDIA_PROCESS_PROCESS_FAIL.equals(status)){
            mediaProcess.setStatus(MEDIA_PROCESS_PROCESS_FAIL);
            mediaProcess.setErrormsg(errorMsg);
            int update = mediaProcessMapper.updateById(mediaProcess);
            if(update <= 0){
                return;
            }
        }
        //任务成功
        if(MEDIA_PROCESS_PROCESS_SUCCESS.equals(status)){
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            if(mediaFiles == null){
                return;
            }
            //1. 为处理好的视频添加url
            mediaFiles.setUrl(url);
            int i = mediaFilesMapper.updateById(mediaFiles);
            if(i <= 0){
                return;
            }
            //2. 删除process表中的信息
            mediaProcessMapper.deleteById(taskId);
            //3. 添加到处理记录表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtil.copyProperties(mediaProcess,mediaProcessHistory);
            mediaProcessHistory.setFinishDate(new Date());
            mediaProcessHistory.setUrl(url);
            mediaProcessHistory.setStatus(MEDIA_PROCESS_PROCESS_SUCCESS);
            int insert = mediaProcessHistoryMapper.insert(mediaProcessHistory);
            if(insert <= 0){
                return;
            }
            // --> 个人感觉这里无论视频是处理成功还是处理失败都应该添加到处理历史表中
        }


    }
}




