package com.onlineedu.content.model.dto;

import com.onlineedu.content.model.entities.Teachplan;
import com.onlineedu.content.model.entities.TeachplanMedia;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @Author cheems
 * @Date 2023/1/26 17:47
 */

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    private TeachplanMedia teachplanMedia;

    //子结点
    private List<TeachplanDto> children;

}
