package com.onlineedu.media.controller;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.media.model.dto.RestResponse;
import com.onlineedu.media.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author cheems
 * @Date 2023/2/15 16:04
 */

@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFilesService mediaFilesService;

    @ApiOperation("门户播放视频")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) throws BusinessException {

        String url  = (String) mediaFilesService.getMediaUrlById(mediaId).getData();
        if(url == null){
           throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"视频还未被转码处理");
        }
        return RestResponse.success(url);

    }


}
