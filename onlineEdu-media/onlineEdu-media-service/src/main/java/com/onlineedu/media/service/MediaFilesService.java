package com.onlineedu.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.media.model.dto.QueryMediaParamsDto;
import com.onlineedu.media.model.dto.UploadFileParamsDto;
import com.onlineedu.media.model.dto.UploadFileResultDto;
import com.onlineedu.media.model.entities.MediaFiles;

/**
* @author cheems
* @description 针对表【media_files(媒资信息)】的数据库操作Service
* @createDate 2023-02-01 23:18:26
*/
public interface MediaFilesService extends IService<MediaFiles> {

    Result getMediaFileList(PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) throws  Exception;

    MediaFiles saveFileMessageToDb(Long companyId, String md5, UploadFileParamsDto uploadFileParamsDto, String bucketFiles, String objectName) throws BusinessException;
}
