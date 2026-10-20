# mes-lite · 轻量级 MES 生产执行系统

<p>
  <a href="https://github.com/sss1148/mes-lite/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/sss1148/mes-lite/actions/workflows/ci.yml/badge.svg"></a>
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F">
  <img alt="MyBatis-Plus" src="https://img.shields.io/badge/MyBatis--Plus-3.5.6-1E90FF">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-green">
</p>

> 个人项目（开发中）：以「工单 → 报工 → 批次追溯」为主线的最小可用 MES 后端，
> 目标是能演示、能被追问、能讲清取舍。

- 作者：穆北涵
- 技术栈：Java 17 · Spring Boot 3 · MyBatis-Plus · MySQL 8 / H2 · Redis（缓存，规划中）· Vue3（前端待接）
- 状态：**后端 MVP 骨架已完成**，前端与看板开发中

## 1. 它解决什么问题

制造车间里最常见的三件事：**工单下发后进度看不见、报工数据重复或对不上、出了质量问题查不到料从哪来**。
mes-lite 用最小的模型把这条链跑通：

```
ERP/计划 ──► 工单(DISPATCHED) ──► 开工(IN_PROGRESS) ──► 报工(幂等) ──► 完工(COMPLETED) ──► 关闭(CLOSED)
                 │                      │                    │
                 └──── 投料(原料批次) ───┴──── 批次追溯 ◄─────┘
```

## 2. 已实现（后端）

| 模块 | 能力 | 关键设计 |
|---|---|---|
| 工单 | 创建 / 状态流转 / 分页查询 | 状态机在服务层校验，非法流转直接拒绝 |
| 报工 | 按工单+批次报工、累加进度 | **幂等键唯一约束 + 乐观锁**，重复提交只累计一次 |
| 投料 | 成品批次关联原料批次 | 反向追溯的链路基础 |
| 批次追溯 | 成品批次 → 原料批次 / 原料批次 → 成品批次 | 双向追溯，索引 `batch_no` / `material_batch_no` |
| OEE | 单工单 OEE（可用率 × 性能 × 合格品率） | 口径写在 `OeeResult` 注释，便于面试讲清假设 |

## 3. 快速开始

```bash
# 1. 编译 + 跑测试
mvn test

# 2. 启动（默认 H2 内存库，自带演示数据，无需装数据库）
mvn spring-boot:run

# 3. 换成 MySQL（先 docker compose up -d mysql）
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

启动后：

- H2 控制台：<http://localhost:8080/h2-console>（JDBC URL：`jdbc:h2:mem:mes`，用户 `sa`，密码空）
- 接口示例见 [docs/api.md](docs/api.md)

## 4. 目录结构

```
mes-lite
├─ src/main/java/com/sss1148/meslite
│  ├─ controller      # 工单 / 追溯 / OEE 接口
│  ├─ service         # 业务逻辑（状态机、幂等报工、追溯）
│  ├─ domain          # 实体 + 工单状态机枚举
│  ├─ mapper          # MyBatis-Plus Mapper
│  ├─ dto / vo        # 入参出参
│  ├─ common          # 统一响应、异常处理
│  └─ config          # 乐观锁 / 分页插件
├─ src/main/resources
│  ├─ schema.sql      # 表结构（H2/MySQL 兼容）
│  ├─ data.sql        # 演示数据
│  ├─ application.yml # 默认 H2
│  └─ application-mysql.yml
├─ src/test/java      # 状态机与幂等报工的单测
├─ docs/              # 设计说明 / 接口文档 / 开发计划
└─ docker-compose.yml # MySQL + Redis
```

## 5. 关键设计（面试可讲）

1. **幂等报工**：`(order_no, batch_no, idempotency_key)` 唯一索引兜底，插入冲突时返回已存在记录；
   为什么不「先查再插」——并发下查与插之间存在窗口，唯一索引才是最终一致的那道闸。
2. **乐观锁回写进度**：工单带 `version`，报工后 `updateById` 校验版本，冲突返回 409 让调用方重试。
3. **状态机**：`WorkOrderStatus` 枚举内置 `canTransitTo`，非法流转在服务层拒绝，不靠 if-else 散落各处。
4. **批次追溯**：用 `material_issue` 存「成品批次—原料批次」关系，正反向各一条索引；
   数据量大时可按批次链做递归 CTE 查询（见 [docs/design.md](docs/design.md)）。

## 6. 开发计划

见 [docs/roadmap.md](docs/roadmap.md)。下一步优先级：

- [ ] Vue3 + Element Plus 前端：工单列表、报工录入、追溯查询、OEE 看板
- [ ] Redis 缓存看板聚合结果（依赖已预留，见 roadmap）
- [ ] JMeter 压测脚本 + 并发报工压测报告（写入 README）
- [ ] Dockerfile + 一键启动
- [ ] 单元测试覆盖到 60% 以上

## 7. 截图

> 前端完成后补：工单列表 / 报工页 / 追溯页 / OEE 看板

## 8. 说明

本项目为个人学习与实践项目，业务模型参考制造业 MES 通用做法（工单、报工、批次追溯、OEE），
数据全部为模拟数据，不涉及任何真实企业数据。
