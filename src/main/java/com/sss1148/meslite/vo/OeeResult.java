package com.sss1148.meslite.vo;

import java.math.BigDecimal;

/**
 * OEE（简化版）：可用率 x 性能开动率 x 合格品率
 * 可用率 = 实际工时 / (计划数量 x 单件标准工时)
 * 性能开动率 = 实际产出 / 计划数量
 * 合格品率 = 良品数 / 实际产出
 */
public class OeeResult {

    private String orderNo;
    private BigDecimal planQty;
    private BigDecimal completedQty;
    private BigDecimal defectQty;
    private BigDecimal workHours;
    private BigDecimal availability;
    private BigDecimal performance;
    private BigDecimal quality;
    private BigDecimal oee;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getPlanQty() {
        return planQty;
    }

    public void setPlanQty(BigDecimal planQty) {
        this.planQty = planQty;
    }

    public BigDecimal getCompletedQty() {
        return completedQty;
    }

    public void setCompletedQty(BigDecimal completedQty) {
        this.completedQty = completedQty;
    }

    public BigDecimal getDefectQty() {
        return defectQty;
    }

    public void setDefectQty(BigDecimal defectQty) {
        this.defectQty = defectQty;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public BigDecimal getAvailability() {
        return availability;
    }

    public void setAvailability(BigDecimal availability) {
        this.availability = availability;
    }

    public BigDecimal getPerformance() {
        return performance;
    }

    public void setPerformance(BigDecimal performance) {
        this.performance = performance;
    }

    public BigDecimal getQuality() {
        return quality;
    }

    public void setQuality(BigDecimal quality) {
        this.quality = quality;
    }

    public BigDecimal getOee() {
        return oee;
    }

    public void setOee(BigDecimal oee) {
        this.oee = oee;
    }
}
