package com.onlineedu.content;

import cn.hutool.json.JSONUtil;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.service.CourseCategoryService;
import com.onlineedu.content.service.TeachplanService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

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

}
