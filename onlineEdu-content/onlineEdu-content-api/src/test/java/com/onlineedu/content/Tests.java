package com.onlineedu.content;

import cn.hutool.json.JSONUtil;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.config.MultipartSupportConfig;
import com.onlineedu.content.service.CourseCategoryService;
import com.onlineedu.content.service.TeachplanService;
import com.onlineedu.content.service.feignClient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @Author cheems
 * @Date 2023/1/20 20:20
 */
@SpringBootTest
public class Tests {

    @Resource
    private CourseCategoryService courseCategoryService;

    @Resource
    private TeachplanService teachplanService;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Test
    public void test1(){
        Result treeNodes = courseCategoryService.getTreeNodes();
        System.out.println(treeNodes.getData());
    }

    @Test
    public void test2(){
//        Result r = teachplanService.getPlanTreeNodes(25L);
//        System.out.println(JSONUtil.toJsonStr(r.getData()));
    }


    @Test
    public void test3(){
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("I:\\test.html"));
        mediaServiceClient.uploadFile(multipartFile,"course","test.html");
    }

}
