# 设计说明

## 1. 领域模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `work_order` | 工单（生产任务） | `order_no` 唯一、`status`、`version` 乐观锁、`plan_qty`、`completed_qty` |
| `work_report` | 报工记录 | `order_no`、`batch_no`、`idempotency_key` 三者唯一 |
| `material_issue` | 投料记录 | `batch_no`（成品批次）、`material_batch_no`（原料批次） |

## 2. 工单状态机

```
DISPATCHED(已下达) ──start──► IN_PROGRESS(生产中) ──complete──► COMPLETED(已完工) ──close──► CLOSED(已关闭)
```

- 只有 `IN_PROGRESS` 允许报工；
- 非法流转（如 `CLOSED → IN_PROGRESS`）直接抛业务异常；
- 新增状态时只需改 `WorkOrderStatus`，其他代码不用动。

## 3. 幂等报工

```
客户端生成 idempotencyKey
        │
        ▼
① 校验工单状态 == IN_PROGRESS
② 查 (order_no, batch_no, idempotency_key)：命中直接返回旧记录
③ 插入 work_report
     └─ DuplicateKeyException（并发重复）→ 再查一次并返回旧记录
④ updateById 回写工单 completed/defect/work_hours（乐观锁，version 冲突返回 409）
```

为什么用唯一索引而不是 `select then insert`：并发下两步之间存在窗口，只有数据库唯一约束能兜住。

## 4. 批次追溯

- 反向（成品 → 原料）：`work_report.batch_no` → 工单 → `material_issue.batch_no` 对应原料批次；
- 正向（原料 → 成品）：`material_issue.material_batch_no` 反查所有成品批次。

批次链较长（多级半成品）时的优化思路：
1. 关联表 + 联合索引（本项目已做）；
2. 递归 CTE 或应用层逐层查询 + 缓存；
3. 数据量>百万级时把追溯关系冗余成闭包表（空间换时间）。

## 5. OEE 口径（简化版）

| 指标 | 公式 | 说明 |
|---|---|---|
| 可用率 | 实际工时 / (计划数量 × 单件标准工时) | 工时来自报工累加 |
| 性能开动率 | 实际产出 / 计划数量 | 未按节拍细分 |
| 合格品率 | (产出 − 不良) / 产出 | 不良来自报工 |

> 真实产线的 OEE 还要考虑计划停机、换型、待料等停机原因；本项目先用可解释的简化口径，
> 后续可在 `work_report` 增加「停机原因」维度再细化。

## 6. 后续扩展点

- 工艺路线 / BOM / 工序：目前用扁平模型，多工序时需要 `routing` + `routing_step`；
- 设备联网：MQTT 接入设备状态，按设备计算 OEE；
- 权限：接 Spring Security 或若依的 RBAC 体系；
- 多租户：`tenant_id` + MyBatis-Plus 多租户插件。
