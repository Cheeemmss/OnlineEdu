package com.onlineedu.base.model;

/**
 * @Author cheems
 * @Date 2023/1/25 15:40
 * 系统中某些实体的状态信息
 */
public abstract class SystemStatus {


    /**
     * 公共属性-使用态
     */
    public static final String PUBLIC_STATUS_USING = "1";

    /**
     * 公共属性-删除态
     */
    public static final String PUBLIC_STATUS_DELETE = "0";

    /**
     * 公共属性-暂时态
     */
    public static final String PUBLIC_STATUS_TEMP = "-1";

    /**
     * 课程收费规则-免费
     */
    public static final String CHARGE_STATUS_FREE = "201000";

    /**
     * 课程收费规则-付费
     */
    public static final String CHARGE_STATUS_NOT_FREE = "201001";

    /**
     * 课程审核状态-审核未通过
     */
    public static final String AUDIT_STATUS_NOT_PASS = "202001";

    /**
     * 课程审核状态-未提交审核
     */
    public static final String AUDIT_STATUS_NOT_COMMIT = "202002";

    /**
     * 课程审核状态-已提交审核
     */
    public static final String AUDIT_STATUS_COMMITTED = "202003";

    /**
     * 课程审核状态-审核通过
     */
    public static final String AUDIT_STATUS_PASSED = "202004";

    /**
     * 课程发布状态-未发布
     */
    public static final String PUBLISH_STATUS_NOT_PUBLISH = "203001";

    /**
     * 课程发布状态-已发布
     */
    public static final String PUBLISH_STATUS_PUBLISHED = "203002";

    /**
     * 课程发布状态-下线
     */
    public static final String PUBLISH_STATUS_DOWN = "203003";

    /**
     * 文件类型-图片
     */
    public static final String FILE_TYPE_IMG = "001001";

    /**
     * 文件类型-视频
     */
    public static final String FILE_TYPE_VIDEO = "001002";

    /**
     * 文件类型-其他
     */
    public static final String FILE_TYPE_OTHER = "001003";

    /**
     * 待处理文件状态-未处理
     */
    public static final String MEDIA_PROCESS_UN_PROCESS = "1";


    /**
     * 待处理文件状态-处理成功
     */
    public static final String MEDIA_PROCESS_PROCESS_SUCCESS = "2";

    /**
     * 待处理文件状态-处理失败
     */
    public static final String MEDIA_PROCESS_PROCESS_FAIL = "3";


}
