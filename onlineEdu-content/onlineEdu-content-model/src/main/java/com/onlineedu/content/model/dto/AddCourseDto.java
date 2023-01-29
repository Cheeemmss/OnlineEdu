package com.onlineedu.content.model.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class AddCourseDto {

      /**
       * 课程主键Id
       */
      private Long id;

      @NotEmpty(message = "课程名称不能为空")
      private String name;

      @NotEmpty(message = "适用人群不能为空")
      private String users;

      @ApiModelProperty(value = "课程标签")
      private String tags;

      @NotEmpty(message = "课程分类不能为空")
      private String mt;

      @NotEmpty(message = "课程分类不能为空")
      private String st;

      @NotEmpty(message = "课程等级不能为空")
      private String grade;

      @NotEmpty(message = "课程类型不能为空")
      private String teachmode;

      private String description;

      private String pic;

      @NotEmpty(message = "收费规则不能为空")
      private String charge;

      private Float price;

      private Float originalPrice;

      private String qq;

      private String wechat;

      private String phone;

      private Integer validDays;
}
