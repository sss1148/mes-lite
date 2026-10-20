package com.sss1148.meslite.domain;

/**
 * 工单状态机：已下达 -> 生产中 -> 已完工 -> 已关闭
 * 非法流转在服务层拦截，不依赖数据库约束。
 */
public enum WorkOrderStatus {

    DISPATCHED("已下达"),
    IN_PROGRESS("生产中"),
    COMPLETED("已完工"),
    CLOSED("已关闭");

    private final String label;

    WorkOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canTransitTo(WorkOrderStatus target) {
        return switch (this) {
            case DISPATCHED -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == COMPLETED;
            case COMPLETED -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
