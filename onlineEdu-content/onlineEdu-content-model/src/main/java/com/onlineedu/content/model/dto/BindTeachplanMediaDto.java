package com.onlineedu.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author cheems
 * @Date 2023/2/13 18:39
 */

@Data
public class BindTeachplanMediaDto {

    private String mediaId;

    private String fileName;

    private Long teachplanId;

}
