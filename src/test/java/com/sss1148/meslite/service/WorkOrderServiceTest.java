package com.sss1148.meslite.service;

import com.sss1148.meslite.common.BizException;
import com.sss1148.meslite.domain.WorkOrder;
import com.sss1148.meslite.domain.WorkOrderStatus;
import com.sss1148.meslite.domain.WorkReport;
import com.sss1148.meslite.dto.CreateWorkOrderRequest;
import com.sss1148.meslite.dto.ReportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class WorkOrderServiceTest {

    @Autowired
    private WorkOrderService workOrderService;

    private CreateWorkOrderRequest newOrder(String orderNo) {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setOrderNo(orderNo);
        req.setProductCode("P-TEST");
        req.setProductName("测试件");
        req.setPlanQty(new BigDecimal("10"));
        req.setStdHoursPerUnit(new BigDecimal("0.5"));
        return req;
    }

    private ReportRequest newReport(String batchNo, String idemKey, String qty) {
        ReportRequest req = new ReportRequest();
        req.setBatchNo(batchNo);
        req.setIdempotencyKey(idemKey);
        req.setReportQty(new BigDecimal(qty));
        req.setDefectQty(BigDecimal.ZERO);
        req.setWorkHours(new BigDecimal("1"));
        req.setOperator("tester");
        return req;
    }

    @Test
    void 状态机_未开始报工会被拒绝() {
        workOrderService.create(newOrder("WO-T-001"));
        BizException e = assertThrows(BizException.class,
                () -> workOrderService.report("WO-T-001", newReport("B-T-001", "k1", "1")));
        assertTrue(e.getMessage().contains("生产中"));
    }

    @Test
    void 状态机_正常流转到已关闭() {
        workOrderService.create(newOrder("WO-T-002"));
        assertEquals(WorkOrderStatus.IN_PROGRESS.name(),
                workOrderService.transit("WO-T-002", WorkOrderStatus.IN_PROGRESS).getStatus());
        assertEquals(WorkOrderStatus.COMPLETED.name(),
                workOrderService.transit("WO-T-002", WorkOrderStatus.COMPLETED).getStatus());
        WorkOrder closed = workOrderService.transit("WO-T-002", WorkOrderStatus.CLOSED);
        assertEquals(WorkOrderStatus.CLOSED.name(), closed.getStatus());
        assertThrows(BizException.class,
                () -> workOrderService.transit("WO-T-002", WorkOrderStatus.IN_PROGRESS));
    }

    @Test
    void 报工_同一幂等键重复提交只累计一次() {
        workOrderService.create(newOrder("WO-T-003"));
        workOrderService.transit("WO-T-003", WorkOrderStatus.IN_PROGRESS);

        WorkReport first = workOrderService.report("WO-T-003", newReport("B-T-003", "idem-1", "2"));
        WorkReport second = workOrderService.report("WO-T-003", newReport("B-T-003", "idem-1", "2"));

        assertEquals(first.getId(), second.getId());
        WorkOrder wo = workOrderService.getByOrderNo("WO-T-003");
        assertEquals(0, new BigDecimal("2").compareTo(wo.getCompletedQty()));
    }

    @Test
    void 报工_不同幂等键正常累计() {
        workOrderService.create(newOrder("WO-T-004"));
        workOrderService.transit("WO-T-004", WorkOrderStatus.IN_PROGRESS);
        workOrderService.report("WO-T-004", newReport("B-T-004", "idem-a", "3"));
        workOrderService.report("WO-T-004", newReport("B-T-004", "idem-b", "4"));
        WorkOrder wo = workOrderService.getByOrderNo("WO-T-004");
        assertEquals(0, new BigDecimal("7").compareTo(wo.getCompletedQty()));
    }
}
