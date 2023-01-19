package com.onlineedu.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @Author cheems
 * @Date 2023/1/19 13:05
 */

@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;

    //课程名称
    private String courseName;

    //发布状态
    private String publishStatus;

}