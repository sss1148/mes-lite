package com.sss1148.meslite.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sss1148.meslite.common.ApiResponse;
import com.sss1148.meslite.domain.MaterialIssue;
import com.sss1148.meslite.domain.WorkOrder;
import com.sss1148.meslite.domain.WorkOrderStatus;
import com.sss1148.meslite.domain.WorkReport;
import com.sss1148.meslite.dto.CreateWorkOrderRequest;
import com.sss1148.meslite.dto.MaterialIssueRequest;
import com.sss1148.meslite.dto.ReportRequest;
import com.sss1148.meslite.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public ApiResponse<WorkOrder> create(@Valid @RequestBody CreateWorkOrderRequest req) {
        return ApiResponse.ok(workOrderService.create(req));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<WorkOrder> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(workOrderService.getByOrderNo(orderNo));
    }

    @GetMapping
    public ApiResponse<Page<WorkOrder>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String status) {
        return ApiResponse.ok(workOrderService.page(page, size, status));
    }

    @PostMapping("/{orderNo}/start")
    public ApiResponse<WorkOrder> start(@PathVariable String orderNo) {
        return ApiResponse.ok(workOrderService.transit(orderNo, WorkOrderStatus.IN_PROGRESS));
    }

    @PostMapping("/{orderNo}/report")
    public ApiResponse<WorkReport> report(@PathVariable String orderNo,
                                          @Valid @RequestBody ReportRequest req) {
        return ApiResponse.ok(workOrderService.report(orderNo, req));
    }

    @PostMapping("/{orderNo}/complete")
    public ApiResponse<WorkOrder> complete(@PathVariable String orderNo) {
        return ApiResponse.ok(workOrderService.transit(orderNo, WorkOrderStatus.COMPLETED));
    }

    @PostMapping("/{orderNo}/close")
    public ApiResponse<WorkOrder> close(@PathVariable String orderNo) {
        return ApiResponse.ok(workOrderService.transit(orderNo, WorkOrderStatus.CLOSED));
    }

    @PostMapping("/{orderNo}/batches/{batchNo}/materials")
    public ApiResponse<MaterialIssue> issueMaterial(@PathVariable String orderNo,
                                                    @PathVariable String batchNo,
                                                    @Valid @RequestBody MaterialIssueRequest req) {
        return ApiResponse.ok(workOrderService.issueMaterial(orderNo, batchNo, req));
    }
}
