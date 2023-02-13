package com.onlineedu.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.media.model.dto.QueryMediaParamsDto;
import com.onlineedu.media.model.dto.UploadFileParamsDto;
import com.onlineedu.media.model.dto.UploadFileResultDto;
import com.onlineedu.media.model.entities.MediaFiles;

import java.io.File;

/**
* @author cheems
* @description 针对表【media_files(媒资信息)】的数据库操作Service
* @createDate 2023-02-01 23:18:26
*/
public interface MediaFilesService extends IService<MediaFiles> {

    Result getMediaFileList(PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) throws  Exception;

    MediaFiles saveFileMessageToDb(Long companyId, String md5, UploadFileParamsDto uploadFileParamsDto, String bucketFiles, String objectName) throws BusinessException;

    Result checkFileIsExistByMd5(String fileMd5);

    Result checkChunkFileIsExistByMd5(String fileMd5,Integer chunkIndex) throws BusinessException;

    Result uploadChunkFile(byte[] bytes, Integer chunkIndex, String fileMd5) throws BusinessException;

    Result mergeChunkFiles(Long companyId,String fileMd5, Integer chunkNum,UploadFileParamsDto uploadFileParamsDto) throws BusinessException;

    Result getMediaUrlById(String mediaId) throws BusinessException;

    File downloadFileFromMinio(File file, String bucket, String objectName) throws BusinessException;

    public void uploadFileToMinio(String filePath, String bucketName, String objectName) throws Exception;

    Result getAuditedMediasList(Long companyId,String mediaName);
}
