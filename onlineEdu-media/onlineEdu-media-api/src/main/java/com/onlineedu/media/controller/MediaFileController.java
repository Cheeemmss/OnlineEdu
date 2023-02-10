package com.onlineedu.media.controller;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.base.model.SystemStatus;
import com.onlineedu.media.model.dto.QueryMediaParamsDto;
import com.onlineedu.media.model.dto.UploadFileParamsDto;
import com.onlineedu.media.model.dto.UploadFileResultDto;
import com.onlineedu.media.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

import static com.onlineedu.base.model.SystemStatus.FILE_TYPE_IMG;
import static com.onlineedu.base.model.SystemStatus.FILE_TYPE_OTHER;

/**
 * @Author cheems
 * @Date 2023/2/2 13:47
 */
@Api(tags = "媒资模块")
@Slf4j
@RestController
public class MediaFileController {

    @Resource
    private MediaFilesService mediaFilesService;

    @ApiOperation("上传的媒资信息分页")
    @PostMapping("/list")
    public Result list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        return mediaFilesService.getMediaFileList(pageParams,queryMediaParamsDto);
    }

    @ApiOperation("上传文件(图片&&其他)")
    @PostMapping(value = "/upload/file",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result upload(@RequestPart("file") MultipartFile multipartFile,
                         @RequestParam(value = "folder",required = false) String folder,
                         @RequestParam(value = "objectName",required = false) String objectName) throws Exception {
        Long companyId = 12L;

        byte[] bytes = multipartFile.getBytes();
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(multipartFile.getOriginalFilename());
        uploadFileParamsDto.setFileSize(multipartFile.getSize());
        String contentType = multipartFile.getContentType();
        uploadFileParamsDto.setContentType(contentType);
        uploadFileParamsDto.setRemark("");
        if (contentType != null) {
            if(contentType.contains("image")){
                uploadFileParamsDto.setFileType(FILE_TYPE_IMG); //图片
            }else{
                uploadFileParamsDto.setFileType(FILE_TYPE_OTHER); //其它
            }
        }
        UploadFileResultDto uploadFileResultDto = mediaFilesService.upload(companyId,uploadFileParamsDto,bytes,folder,objectName);
        return Result.success(uploadFileResultDto);
    }

    @ApiOperation("文件预览")
    @GetMapping("/preview/{mediaId}")
    public Result mediaPreview(@PathVariable("mediaId") String mediaId) throws BusinessException {
        return mediaFilesService.getMediaUrlById(mediaId);
    }

}
