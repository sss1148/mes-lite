# 接口文档

基础地址：`http://localhost:8080`，统一响应：`{ "code": 0, "message": "ok", "data": ... }`

## 工单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/work-orders` | 创建工单（创建即「已下达」） |
| GET | `/api/work-orders/{orderNo}` | 工单详情 |
| GET | `/api/work-orders?page=1&size=10&status=IN_PROGRESS` | 分页查询 |
| POST | `/api/work-orders/{orderNo}/start` | 开工（已下达 → 生产中） |
| POST | `/api/work-orders/{orderNo}/report` | 报工（幂等） |
| POST | `/api/work-orders/{orderNo}/complete` | 完工 |
| POST | `/api/work-orders/{orderNo}/close` | 关闭 |
| POST | `/api/work-orders/{orderNo}/batches/{batchNo}/materials` | 投料 |

## 追溯与看板

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/trace/batches/{batchNo}` | 反向追溯：成品批次 → 原料批次 + 报工记录 |
| GET | `/api/trace/materials/{materialBatchNo}` | 正向追溯：原料批次 → 成品批次 |
| GET | `/api/dashboard/oee/{orderNo}` | 单工单 OEE |

## 示例

```bash
# 1. 创建工单
curl -X POST http://localhost:8080/api/work-orders \
  -H 'Content-Type: application/json' \
  -d '{"orderNo":"WO20261020003","productCode":"P-1001","productName":"法兰盘 A","planQty":100,"stdHoursPerUnit":0.5}'

# 2. 开工
curl -X POST http://localhost:8080/api/work-orders/WO20261020003/start

# 3. 报工（同一 idempotencyKey 重复调用只会累计一次）
curl -X POST http://localhost:8080/api/work-orders/WO20261020003/report \
  -H 'Content-Type: application/json' \
  -d '{"batchNo":"B20261020-01","idempotencyKey":"req-0001","reportQty":30,"defectQty":1,"workHours":15,"operator":"张工","equipmentCode":"EQ-07"}'

# 4. 投料（成品批次 ← 原料批次）
curl -X POST http://localhost:8080/api/work-orders/WO20261020003/batches/B20261020-01/materials \
  -H 'Content-Type: application/json' \
  -d '{"materialCode":"M-2001","materialName":"铸坯","materialBatchNo":"RM20261015-08","qty":35,"operator":"李工"}'

# 5. 反向追溯
curl http://localhost:8080/api/trace/batches/B20261020-01

# 6. OEE
curl http://localhost:8080/api/dashboard/oee/WO20261020003
```
