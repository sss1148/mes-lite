package com.sss1148.meslite.service;

import com.sss1148.meslite.domain.WorkOrder;
import com.sss1148.meslite.vo.OeeResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DashboardService {

    private final WorkOrderService workOrderService;

    public DashboardService(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    /**
     * OEE（简化版，口径写在 OeeResult 注释里）：
     * 可用率 = 实际工时 / (计划数量 × 单件标准工时)
     * 性能开动率 = 实际产出 / 计划数量
     * 合格品率 = 良品数 / 实际产出
     */
    public OeeResult oee(String orderNo) {
        WorkOrder wo = workOrderService.getByOrderNo(orderNo);
        BigDecimal planHours = wo.getPlanQty().multiply(wo.getStdHoursPerUnit());
        BigDecimal availability = ratio(wo.getWorkHours(), planHours);
        BigDecimal performance = ratio(wo.getCompletedQty(), wo.getPlanQty());
        BigDecimal goodQty = wo.getCompletedQty().subtract(wo.getDefectQty());
        BigDecimal quality = ratio(goodQty, wo.getCompletedQty());

        OeeResult r = new OeeResult();
        r.setOrderNo(orderNo);
        r.setPlanQty(wo.getPlanQty());
        r.setCompletedQty(wo.getCompletedQty());
        r.setDefectQty(wo.getDefectQty());
        r.setWorkHours(wo.getWorkHours());
        r.setAvailability(availability);
        r.setPerformance(performance);
        r.setQuality(quality);
        r.setOee(availability.multiply(performance).multiply(quality).setScale(4, RoundingMode.HALF_UP));
        return r;
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
    }
}
