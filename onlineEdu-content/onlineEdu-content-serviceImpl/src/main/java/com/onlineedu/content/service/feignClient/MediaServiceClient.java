package com.onlineedu.content.service.feignClient;

import com.onlineedu.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author cheems
 * @Date 2023/2/27 17:19
 */

//fallback方法无法获取调用时的异常 fallbackFactory可以
@FeignClient(value = "media-api",configuration = {MultipartSupportConfig.class},
        fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    @PostMapping(value = "/media/upload/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile upload,
                      @RequestParam(value = "folder",required=false) String folder,
                      @RequestParam(value = "objectName",required=false) String objectName);

}
