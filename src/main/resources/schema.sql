-- 轻量 MES 表结构（H2 / MySQL 兼容写法）
drop table if exists work_order;
drop table if exists work_report;
drop table if exists material_issue;

create table work_order (
    id                 bigint auto_increment primary key,
    order_no           varchar(32)  not null,
    product_code       varchar(32)  not null,
    product_name       varchar(64),
    plan_qty           decimal(18,3) not null,
    completed_qty      decimal(18,3) not null default 0,
    defect_qty         decimal(18,3) not null default 0,
    work_hours         decimal(18,3) not null default 0,
    std_hours_per_unit decimal(10,4) not null,
    status             varchar(20)  not null,
    priority           int          not null default 5,
    plan_start_date    date,
    plan_end_date      date,
    version            int          not null default 0,
    created_at         timestamp,
    updated_at         timestamp,
    constraint uk_work_order_no unique (order_no)
);

create table work_report (
    id              bigint auto_increment primary key,
    order_no        varchar(32)  not null,
    batch_no        varchar(32)  not null,
    idempotency_key varchar(64)  not null,
    report_qty      decimal(18,3) not null,
    defect_qty      decimal(18,3) not null default 0,
    work_hours      decimal(18,3) not null default 0,
    operator        varchar(32),
    equipment_code  varchar(32),
    remark          varchar(255),
    reported_at     timestamp,
    constraint uk_report_idem unique (order_no, batch_no, idempotency_key)
);

create index idx_report_batch on work_report (batch_no);
create index idx_report_order on work_report (order_no);

create table material_issue (
    id               bigint auto_increment primary key,
    order_no         varchar(32)  not null,
    batch_no         varchar(32)  not null,
    material_code    varchar(32)  not null,
    material_name    varchar(64),
    material_batch_no varchar(32) not null,
    qty              decimal(18,3) not null,
    operator         varchar(32),
    issued_at        timestamp
);

create index idx_issue_batch on material_issue (batch_no);
create index idx_issue_material_batch on material_issue (material_batch_no);
