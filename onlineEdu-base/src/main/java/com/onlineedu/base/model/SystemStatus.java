package com.onlineedu.base.model;

/**
 * @Author cheems
 * @Date 2023/1/25 15:40
 * 系统中某些实体的状态信息
 */
public abstract class SystemStatus {

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




}
