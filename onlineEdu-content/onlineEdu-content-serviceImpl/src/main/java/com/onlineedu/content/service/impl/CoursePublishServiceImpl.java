package com.onlineedu.content.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.content.config.MultipartSupportConfig;
import com.onlineedu.content.mapper.CourseBaseMapper;
import com.onlineedu.content.mapper.CourseMarketMapper;
import com.onlineedu.content.mapper.CoursePublishMapper;
import com.onlineedu.content.mapper.CoursePublishPreMapper;
import com.onlineedu.content.model.dto.CourseBaseInfoDto;
import com.onlineedu.content.model.dto.CoursePreviewDto;
import com.onlineedu.content.model.dto.TeachplanDto;
import com.onlineedu.content.model.entities.*;
import com.onlineedu.content.service.CourseBaseService;
import com.onlineedu.content.service.CoursePublishService;
import com.onlineedu.content.service.TeachplanService;
import com.onlineedu.content.service.feignClient.MediaServiceClient;
import com.onlineedu.content.service.handler.CoursePublishTask;
import com.onlineedu.messagesdk.model.po.MqMessage;
import com.onlineedu.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author cheems
* @description 针对表【course_publish(课程发布)】的数据库操作Service实现
* @createDate 2023-01-19 14:15:57
*/

@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish>
    implements CoursePublishService{

    @Resource
    private CourseBaseService courseBaseService;

    @Resource
    TeachplanService teachplanService;

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto

    //获取课程的基本信息 营销信息 课程计划 (老师信息)
    getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = (CourseBaseInfoDto) courseBaseService.getCourseBaseInfoById(courseId).getData();
        List<TeachplanDto> planTreeNodes = teachplanService.getPlanTreeNodes(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(planTreeNodes);
        return coursePreviewDto;
    }

    //提交审核
    @Override
    public void commitAudit(Long companyId, Long courseId) throws BusinessException {

        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if("202003".equals(auditStatus)){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if(StrUtil.isEmpty(courseBase.getPic())){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"提交失败，请上传课程图片");
        }

        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = (CourseBaseInfoDto) courseBaseService.getCourseBaseInfoById(courseId).getData();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.getPlanTreeNodes(courseId);
        if(teachplanTree.size()<=0){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"提交失败，还没有添加课程计划");
        }
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(new Date());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    //发布课程
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) throws BusinessException {

        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"不允许提交其它机构的课程。");
        }


        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if(!"202004".equals(auditStatus)){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    private void saveCoursePublishMessage(Long courseId) throws BusinessException {
        MqMessage mqMessage = mqMessageService.addMessage(CoursePublishTask.MESSAGE_TYPE, String.valueOf(courseId), null, null);
        if(mqMessage==null){
           throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程发布任务加入失败");
        }
    }

    private void saveCoursePublish(Long courseId) throws BusinessException {
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 根据ftl文件生成课程的静态文件
     * @param courseId
     * @return 生成成功返回生成的文件对应的File对象 生成失败返回Null
     */
    @Override
    public File generateCourseHtml(Long courseId) {

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
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

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

        return htmlFile;
    }

    //将生成的课程静态html页面上传到minio
    @Override
    public void uploadCourseHtml(Long courseId, File file) throws BusinessException {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String result = mediaServiceClient.uploadFile(multipartFile, "course", courseId + ".html");
        if(result == null){
            throw new BusinessException(SystemCode.CODE_UNKOWN_ERROR,"远程调用媒资上传静态页面失败");
        }
    }
}




