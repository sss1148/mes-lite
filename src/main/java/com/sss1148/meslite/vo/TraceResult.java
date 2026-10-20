package com.sss1148.meslite.vo;

import com.sss1148.meslite.domain.MaterialIssue;
import com.sss1148.meslite.domain.WorkReport;

import java.util.List;

/**
 * 批次追溯结果：反向（成品批次 -> 原料批次 + 报工记录）
 */
public class TraceResult {

    private String orderNo;
    private String batchNo;
    private String productCode;
    private String productName;
    private List<WorkReport> reports;
    private List<MaterialIssue> materials;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<WorkReport> getReports() {
        return reports;
    }

    public void setReports(List<WorkReport> reports) {
        this.reports = reports;
    }

    public List<MaterialIssue> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialIssue> materials) {
        this.materials = materials;
    }
}
