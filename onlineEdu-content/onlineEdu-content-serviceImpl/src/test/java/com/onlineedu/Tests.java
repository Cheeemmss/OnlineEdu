package com.onlineedu;

import com.onlineedu.content.config.MultipartSupportConfig;
import com.onlineedu.content.model.dto.CoursePreviewDto;
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
 * @Date 2023/1/29 17:48
 */

class Tests {


}