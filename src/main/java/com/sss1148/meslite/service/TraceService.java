package com.sss1148.meslite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sss1148.meslite.domain.MaterialIssue;
import com.sss1148.meslite.domain.WorkOrder;
import com.sss1148.meslite.domain.WorkReport;
import com.sss1148.meslite.mapper.MaterialIssueMapper;
import com.sss1148.meslite.mapper.WorkOrderMapper;
import com.sss1148.meslite.mapper.WorkReportMapper;
import com.sss1148.meslite.vo.TraceResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkReportMapper workReportMapper;
    private final MaterialIssueMapper materialIssueMapper;

    public TraceService(WorkOrderMapper workOrderMapper,
                        WorkReportMapper workReportMapper,
                        MaterialIssueMapper materialIssueMapper) {
        this.workOrderMapper = workOrderMapper;
        this.workReportMapper = workReportMapper;
        this.materialIssueMapper = materialIssueMapper;
    }

    /**
     * 反向追溯：成品批次 -> 报工记录 + 投入的原料批次
     */
    public TraceResult traceByBatch(String batchNo) {
        List<WorkReport> reports = workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>()
                .eq(WorkReport::getBatchNo, batchNo)
                .orderByAsc(WorkReport::getReportedAt));
        TraceResult result = new TraceResult();
        result.setBatchNo(batchNo);
        result.setReports(reports);
        if (reports.isEmpty()) {
            return result;
        }
        String orderNo = reports.get(0).getOrderNo();
        result.setOrderNo(orderNo);
        WorkOrder wo = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getOrderNo, orderNo));
        if (wo != null) {
            result.setProductCode(wo.getProductCode());
            result.setProductName(wo.getProductName());
        }
        result.setMaterials(materialIssueMapper.selectList(new LambdaQueryWrapper<MaterialIssue>()
                .eq(MaterialIssue::getBatchNo, batchNo)
                .orderByAsc(MaterialIssue::getIssuedAt)));
        return result;
    }

    /**
     * 正向追溯：原料批次 -> 用到它的所有成品批次
     */
    public List<MaterialIssue> traceByMaterialBatch(String materialBatchNo) {
        return materialIssueMapper.selectList(new LambdaQueryWrapper<MaterialIssue>()
                .eq(MaterialIssue::getMaterialBatchNo, materialBatchNo)
                .orderByAsc(MaterialIssue::getIssuedAt));
    }
}
