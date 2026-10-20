package com.sss1148.meslite.controller;

import com.sss1148.meslite.common.ApiResponse;
import com.sss1148.meslite.domain.MaterialIssue;
import com.sss1148.meslite.service.TraceService;
import com.sss1148.meslite.vo.TraceResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    /**
     * 反向追溯：从成品批次查投入的原料批次与报工记录
     */
    @GetMapping("/batches/{batchNo}")
    public ApiResponse<TraceResult> traceByBatch(@PathVariable String batchNo) {
        return ApiResponse.ok(traceService.traceByBatch(batchNo));
    }

    /**
     * 正向追溯：从原料批次查流向的成品批次
     */
    @GetMapping("/materials/{materialBatchNo}")
    public ApiResponse<List<MaterialIssue>> traceByMaterial(@PathVariable String materialBatchNo) {
        return ApiResponse.ok(traceService.traceByMaterialBatch(materialBatchNo));
    }
}
