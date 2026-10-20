package com.sss1148.meslite.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReportRequest {

    @NotBlank(message = "不能为空")
    private String batchNo;

    @NotBlank(message = "不能为空：幂等键由客户端生成，同一请求重试必须复用")
    private String idempotencyKey;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.001", message = "必须大于 0")
    private BigDecimal reportQty;

    private BigDecimal defectQty;

    private BigDecimal workHours;

    private String operator;
    private String equipmentCode;
    private String remark;

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getReportQty() {
        return reportQty;
    }

    public void setReportQty(BigDecimal reportQty) {
        this.reportQty = reportQty;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
