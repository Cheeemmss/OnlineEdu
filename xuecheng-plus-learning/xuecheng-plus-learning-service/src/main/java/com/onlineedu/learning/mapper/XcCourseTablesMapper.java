package com.onlineedu.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineedu.learning.model.dto.MyCourseTableItemDto;
import com.onlineedu.learning.model.dto.MyCourseTableParams;
import com.onlineedu.learning.model.po.XcCourseTables;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface XcCourseTablesMapper extends BaseMapper<XcCourseTables> {

    public List<MyCourseTableItemDto> myCourseTables( MyCourseTableParams params);
    public int myCourseTablesCount( MyCourseTableParams params);

}
