package com.sss1148.meslite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sss1148.meslite.common.BizException;
import com.sss1148.meslite.domain.MaterialIssue;
import com.sss1148.meslite.domain.WorkOrder;
import com.sss1148.meslite.domain.WorkOrderStatus;
import com.sss1148.meslite.domain.WorkReport;
import com.sss1148.meslite.dto.CreateWorkOrderRequest;
import com.sss1148.meslite.dto.MaterialIssueRequest;
import com.sss1148.meslite.dto.ReportRequest;
import com.sss1148.meslite.mapper.MaterialIssueMapper;
import com.sss1148.meslite.mapper.WorkOrderMapper;
import com.sss1148.meslite.mapper.WorkReportMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkReportMapper workReportMapper;
    private final MaterialIssueMapper materialIssueMapper;

    public WorkOrderService(WorkOrderMapper workOrderMapper,
                            WorkReportMapper workReportMapper,
                            MaterialIssueMapper materialIssueMapper) {
        this.workOrderMapper = workOrderMapper;
        this.workReportMapper = workReportMapper;
        this.materialIssueMapper = materialIssueMapper;
    }

    public WorkOrder create(CreateWorkOrderRequest req) {
        Long exists = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getOrderNo, req.getOrderNo()));
        if (exists != null && exists > 0) {
            throw new BizException("工单号已存在：" + req.getOrderNo());
        }
        WorkOrder wo = new WorkOrder();
        wo.setOrderNo(req.getOrderNo());
        wo.setProductCode(req.getProductCode());
        wo.setProductName(req.getProductName());
        wo.setPlanQty(req.getPlanQty());
        wo.setCompletedQty(BigDecimal.ZERO);
        wo.setDefectQty(BigDecimal.ZERO);
        wo.setWorkHours(BigDecimal.ZERO);
        wo.setStdHoursPerUnit(req.getStdHoursPerUnit());
        wo.setPriority(req.getPriority() == null ? 5 : req.getPriority());
        wo.setPlanStartDate(req.getPlanStartDate());
        wo.setPlanEndDate(req.getPlanEndDate());
        // 新建即视为已下达（简化：不单独做草稿态）
        wo.setStatus(WorkOrderStatus.DISPATCHED.name());
        wo.setVersion(0);
        wo.setCreatedAt(LocalDateTime.now());
        wo.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.insert(wo);
        return wo;
    }

    @Transactional
    public WorkOrder transit(String orderNo, WorkOrderStatus target) {
        WorkOrder wo = getByOrderNo(orderNo);
        WorkOrderStatus current = wo.statusEnum();
        if (!current.canTransitTo(target)) {
            throw new BizException("非法状态流转：" + current.getLabel() + " -> " + target.getLabel());
        }
        wo.setStatus(target.name());
        wo.setUpdatedAt(LocalDateTime.now());
        int rows = workOrderMapper.updateById(wo);
        if (rows == 0) {
            throw new BizException(409, "并发冲突：工单已被他人修改，请重试");
        }
        return getByOrderNo(orderNo);
    }

    /**
     * 报工：幂等键 (orderNo + batchNo + idempotencyKey) 唯一约束 + 乐观锁回写工单
     */
    @Transactional
    public WorkReport report(String orderNo, ReportRequest req) {
        WorkOrder wo = getByOrderNo(orderNo);
        if (wo.statusEnum() != WorkOrderStatus.IN_PROGRESS) {
            throw new BizException("只有「生产中」的工单可以报工，当前状态：" + wo.statusEnum().getLabel());
        }
        WorkReport existed = findReport(orderNo, req.getBatchNo(), req.getIdempotencyKey());
        if (existed != null) {
            return existed;
        }

        WorkReport report = new WorkReport();
        report.setOrderNo(orderNo);
        report.setBatchNo(req.getBatchNo());
        report.setIdempotencyKey(req.getIdempotencyKey());
        report.setReportQty(req.getReportQty());
        report.setDefectQty(req.getDefectQty() == null ? BigDecimal.ZERO : req.getDefectQty());
        report.setWorkHours(req.getWorkHours() == null ? BigDecimal.ZERO : req.getWorkHours());
        report.setOperator(req.getOperator());
        report.setEquipmentCode(req.getEquipmentCode());
        report.setRemark(req.getRemark());
        report.setReportedAt(LocalDateTime.now());
        try {
            workReportMapper.insert(report);
        } catch (DuplicateKeyException e) {
            // 并发重复提交：唯一索引兜底，直接返回已存在记录
            return findReport(orderNo, req.getBatchNo(), req.getIdempotencyKey());
        }

        wo.setCompletedQty(wo.getCompletedQty().add(report.getReportQty()));
        wo.setDefectQty(wo.getDefectQty().add(report.getDefectQty()));
        wo.setWorkHours(wo.getWorkHours().add(report.getWorkHours()));
        wo.setUpdatedAt(LocalDateTime.now());
        int rows = workOrderMapper.updateById(wo);
        if (rows == 0) {
            throw new BizException(409, "并发冲突：工单进度回写失败，请重试");
        }
        return report;
    }

    @Transactional
    public MaterialIssue issueMaterial(String orderNo, String batchNo, MaterialIssueRequest req) {
        WorkOrder wo = getByOrderNo(orderNo);
        if (wo.statusEnum() == WorkOrderStatus.CLOSED) {
            throw new BizException("工单已关闭，不能投料");
        }
        MaterialIssue issue = new MaterialIssue();
        issue.setOrderNo(orderNo);
        issue.setBatchNo(batchNo);
        issue.setMaterialCode(req.getMaterialCode());
        issue.setMaterialName(req.getMaterialName());
        issue.setMaterialBatchNo(req.getMaterialBatchNo());
        issue.setQty(req.getQty());
        issue.setOperator(req.getOperator());
        issue.setIssuedAt(LocalDateTime.now());
        materialIssueMapper.insert(issue);
        return issue;
    }

    public WorkOrder getByOrderNo(String orderNo) {
        WorkOrder wo = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getOrderNo, orderNo));
        if (wo == null) {
            throw new BizException(404, "工单不存在：" + orderNo);
        }
        return wo;
    }

    public Page<WorkOrder> page(long page, long size, String status) {
        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<WorkOrder>()
                .eq(status != null && !status.isBlank(), WorkOrder::getStatus, status)
                .orderByDesc(WorkOrder::getCreatedAt);
        return workOrderMapper.selectPage(new Page<>(page, size), qw);
    }

    private WorkReport findReport(String orderNo, String batchNo, String idempotencyKey) {
        return workReportMapper.selectOne(new LambdaQueryWrapper<WorkReport>()
                .eq(WorkReport::getOrderNo, orderNo)
                .eq(WorkReport::getBatchNo, batchNo)
                .eq(WorkReport::getIdempotencyKey, idempotencyKey));
    }
}
