-- 演示数据：一张在制工单 + 一条报工 + 两条投料
insert into work_order (order_no, product_code, product_name, plan_qty, completed_qty, defect_qty, work_hours,
                        std_hours_per_unit, status, priority, plan_start_date, plan_end_date, version, created_at, updated_at)
values ('WO20261020001', 'P-1001', '法兰盘 A', 100, 0, 0, 0, 0.5, 'IN_PROGRESS', 5,
        current_date, current_date, 0, current_timestamp, current_timestamp);

insert into work_order (order_no, product_code, product_name, plan_qty, completed_qty, defect_qty, work_hours,
                        std_hours_per_unit, status, priority, plan_start_date, plan_end_date, version, created_at, updated_at)
values ('WO20261020002', 'P-1002', '密封圈 B', 200, 0, 0, 0, 0.2, 'DISPATCHED', 3,
        current_date, current_date, 0, current_timestamp, current_timestamp);
