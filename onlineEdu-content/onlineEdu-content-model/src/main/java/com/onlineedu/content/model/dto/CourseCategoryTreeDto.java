package com.onlineedu.content.model.dto;

import com.onlineedu.content.model.entities.CourseCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @Author cheems
 * @Date 2023/1/20 19:52
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    /**
     * 子节点
     */
    List<CourseCategoryTreeDto> children;

    /**
     * 方便前端树形选择使用 值同 id
     */
    String value;

}
