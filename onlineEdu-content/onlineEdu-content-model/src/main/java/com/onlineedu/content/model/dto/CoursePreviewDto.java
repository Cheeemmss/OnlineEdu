package com.onlineedu.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author cheems
 * @Date 2023/2/15 14:36
 */

@Data
@ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加

}
