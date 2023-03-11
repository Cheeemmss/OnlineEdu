package com.onlineedu.content;

import cn.hutool.json.JSONUtil;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.config.MultipartSupportConfig;
import com.onlineedu.content.model.dto.CoursePreviewDto;
import com.onlineedu.content.service.CourseCategoryService;
import com.onlineedu.content.service.CoursePublishService;
import com.onlineedu.content.service.TeachplanService;
import com.onlineedu.content.service.feignClient.MediaServiceClient;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author cheems
 * @Date 2023/1/20 20:20
 */
@Slf4j
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

    @Resource
    private CoursePublishService coursePublishService;

    @Test
    public void generateCourseHtml() {

        //静态化文件
        File htmlFile  = null;
        FileOutputStream outputStream = null;
        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            configuration.setClassForTemplateLoading(this.getClass(),"/templates/");
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(141L);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            outputStream =  new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(htmlFile != null){
                if(htmlFile.exists()){
                    htmlFile.delete();
                }
            }
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
