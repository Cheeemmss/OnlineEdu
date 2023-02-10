package com.onlineedu.media.service.jobhandler;

import com.onlineedu.base.utils.Mp4VideoUtil;
import com.onlineedu.base.utils.VideoUtil;
import com.onlineedu.media.mapper.MediaProcessMapper;
import com.onlineedu.media.model.entities.MediaProcess;
import com.onlineedu.media.service.MediaFilesService;
import com.onlineedu.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.onlineedu.base.model.SystemStatus.MEDIA_PROCESS_PROCESS_FAIL;
import static com.onlineedu.base.model.SystemStatus.MEDIA_PROCESS_PROCESS_SUCCESS;

@Slf4j
@Component
public class MediaProcessHandler {

    @Resource
    private MediaProcessService mediaProcessService;

    @Resource
    private MediaFilesService mediaFilesService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;


    @XxlJob("processMediaHandler")
    public void processMediaHandler(){
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("当前执行器:{}",shardIndex);
        // 查找该执行器需要处理的视频的列表
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, 2);
        int taskCount = mediaProcessList.size();
        if(taskCount == 0){
            return;
        }

        CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        // 开启线程分别对每个视频进行处理(一个线程负责处理一个视频)
        ExecutorService threadPool = Executors.newFixedThreadPool(taskCount);
        for (MediaProcess mediaProcess : mediaProcessList) {
            threadPool.execute(() -> {
                //从minio下载对应的视频
                String fileId = mediaProcess.getFileId();
                String bucket = mediaProcess.getBucket();
                String filePath = mediaProcess.getFilePath();
                File tempFile = null;
                File targetMp4TempFile = null;
                try {
                    tempFile = File.createTempFile("tempProcess", null);
                    targetMp4TempFile = File.createTempFile("targetProcess",".mp4");
                    tempFile = mediaFilesService.downloadFileFromMinio(tempFile, bucket, filePath);
                } catch (Exception e) {
                    log.info("文件转码-文件下载异常,文件id:{}",fileId);
                    e.printStackTrace();
                }

                //下载成功后进行转码
                try {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, tempFile.getAbsolutePath(), targetMp4TempFile.getName(), targetMp4TempFile.getAbsolutePath());
                    String result = mp4VideoUtil.generateMp4();
                    stopWatch.stop();
                    log.info("处理完成,目标文件大小(B): {},耗时: {}",targetMp4TempFile.length(),stopWatch.getTotalTimeSeconds());
                    if(!"success".equals(result)){
                        // 转码失败 修改视频处理状态 保存错误信息
                        log.info(result);
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(),MEDIA_PROCESS_PROCESS_FAIL,fileId,null,result);
                    }
                    // 转码成功
                    // 上传至Minio
                    String objectName = getFilePath(fileId,".mp4");
                    mediaFilesService.uploadFileToMinio(targetMp4TempFile.getAbsolutePath(), bucket,objectName);
                    // 删除在待处理表中的数据 修改状态后保存到转码记录表中 并为mediaFile表中的对应文件设置访问的url
                    String url = "/" + bucket + "/" + objectName;
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(),MEDIA_PROCESS_PROCESS_SUCCESS,fileId,url,result);
                } catch (Exception e) {
                    log.info("视频转码过程异常,文件id:{}",fileId);
                    e.printStackTrace();
                }finally {
                    //删除临时文件
                    if(tempFile.exists()){
                        tempFile.delete();
                    }
                    if(targetMp4TempFile.exists()){
                        targetMp4TempFile.delete();
                    }
                    countDownLatch.countDown(); //无论失败还是成功都要 -1
                }
            });
        }

        try {
            countDownLatch.await(30,TimeUnit.SECONDS);  //阻塞等待任务全部执行完毕(30min兜底)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
