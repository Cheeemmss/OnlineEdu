package com.onlineedu.media.controller;

import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.model.Result;
import com.onlineedu.media.model.dto.UploadFileParamsDto;
import com.onlineedu.media.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

import static com.onlineedu.base.model.SystemStatus.FILE_TYPE_VIDEO;

/**
 * @Author cheems
 * @Date 2023/2/7 14:37
 */

@Api(tags = "大文件上传")
@Slf4j
@RestController
public class BigFileController {

    @Resource
    private MediaFilesService mediaFilesService;

    @ApiOperation("检查文件是否存在")
    @GetMapping("/checkFile/{fileMd5}")
    public Result checkFileIsExist(@PathVariable("fileMd5") String fileMd5){
        return mediaFilesService.checkFileIsExistByMd5(fileMd5);
    }

    @ApiOperation("检查分片文件是否存在")
    @GetMapping(value = "/checkChunk/{fileMd5}/{chunkIndex}")
    public Result checkChunkFileIsExist(@PathVariable("fileMd5") String fileMd5,
                                        @PathVariable("chunkIndex") Integer chunkIndex) throws BusinessException {
        return mediaFilesService.checkChunkFileIsExistByMd5(fileMd5,chunkIndex);
    }

    @ApiOperation("上传分片文件")
    @PostMapping(value = "/uploadChunk",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result uploadChunkFile(@RequestPart("file") MultipartFile file,
                                  @RequestParam("fileMd5") String fileMd5,
                                  @RequestParam("chunkIndex") Integer chunkIndex) throws Exception {
        return mediaFilesService.uploadChunkFile(file.getBytes(),chunkIndex,fileMd5);
    }

    @ApiOperation("合并分片文件")
    @GetMapping("/mergeChunk/{fileMd5}/{fileName}/{chunkNum}")
    public Result mergeChunkFiles(@PathVariable("fileMd5") String fileMd5,
                                  @PathVariable("fileName") String fileName,
                                  @PathVariable("chunkNum") Integer chunkNum) throws BusinessException {
        Long companyId = 12L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType(FILE_TYPE_VIDEO);
        uploadFileParamsDto.setTags("课程视频");
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(fileName);
        return mediaFilesService.mergeChunkFiles(companyId,fileMd5,chunkNum,uploadFileParamsDto);
    }
}
