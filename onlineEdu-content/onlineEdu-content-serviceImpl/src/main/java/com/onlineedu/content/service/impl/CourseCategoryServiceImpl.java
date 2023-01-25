package com.onlineedu.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.model.Result;
import com.onlineedu.content.model.dto.CourseCategoryTreeDto;
import com.onlineedu.content.model.entities.CourseCategory;
import com.onlineedu.content.service.CourseCategoryService;
import com.onlineedu.content.mapper.CourseCategoryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author cheems
* @description 针对表【course_category(课程分类)】的数据库操作Service实现
* @createDate 2023-01-20 19:46:32
*/
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory>
    implements CourseCategoryService{

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    //课程分类根节点
    private static final String ROOT_NODE_ID = "1";

    @Override
    public Result getTreeNodes() {
        List<CourseCategoryTreeDto> allCategory = courseCategoryMapper.selectAllTreeNodes();
        List<CourseCategoryTreeDto> TreeNodes = allCategory.stream()
                .filter(item -> ROOT_NODE_ID.equals(item.getParentid()))
                .map(item -> {
                    item.setChildren(getChildNodes(item, allCategory));
                    return item;
                }).collect(Collectors.toList());
        return Result.success(TreeNodes);
    }

    /**
     * 获取某一个节点(父节点)的所有子节点
     * @param parentNode 父节点
     * @param allCategory 所有节点列表
     * @return 该(父节点)的所有子节点
     */
    private List<CourseCategoryTreeDto> getChildNodes(CourseCategoryTreeDto parentNode,List<CourseCategoryTreeDto> allCategory) {
        List<CourseCategoryTreeDto> childNodes = allCategory.stream()
                .filter(item -> parentNode.getId().equals(item.getParentid()))
                .map(item -> {
                    item.setChildren(getChildNodes(item, allCategory));
                    return item;
                }).collect(Collectors.toList());
        if(childNodes.size() == 0) return null; //方便前端渲染
        return childNodes;
    }
}




