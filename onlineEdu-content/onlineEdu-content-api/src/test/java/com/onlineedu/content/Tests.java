package com.onlineedu.content;

import com.onlineedu.base.model.Result;
import com.onlineedu.content.service.CourseCategoryService;
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

    @Test
    public void test1(){
        Result treeNodes = courseCategoryService.getTreeNodes();
        System.out.println(treeNodes.getData());
    }

}
