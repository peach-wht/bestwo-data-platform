# 微信支付数仓 MVP 开发任务拆解表

## 1. 仓库现状复核

| 维度 | 真实文件 | 现状复核 | 结论 |
|---|---|---|---|
| 后端模块 | `backend/pom.xml` | 当前真实模块有 `common`、`gateway-service`、`auth-service`、`order-service`、`warehouse-service`、`task-service`、`sync-service`。 | 模块结构已经为“业务写侧 + 数仓读侧 + 网关”预留好了。 |
| 项目整体定位 | `README.md` | 仓库明确写的是“微信订单数据接入与数仓分析平台”，但当前仍以骨架为主。 | 这次 MVP 应做真实业务闭环，不应再停留在纯骨架。 |
| gateway 现状 | `backend/gateway-service/src/main/resources/application.yml` | 当前网关只正式路由了 `/api/warehouse/**` 和 `/warehouse/**` 到 `warehouse-service`。 | 网关结构适合继续挂 `/api/orders/**`、`/api/pay/**`、`/api/dw/**`，但需要补路由。 |
| 登录鉴权现状 | `backend/gateway-service/src/main/java/com/bestwo/dataplatform/gateway/security/SecurityConfig.java`、`backend/gateway-service/src/main/java/com/bestwo/dataplatform/gateway/controller/AuthController.java` | 已具备 Session 登录、401/403、`/api/auth/login`、`/api/auth/logout`、`/api/auth/me`。 | 认证不是当前阻塞点，后续只需给支付回调和少量公开接口做白名单。 |
| warehouse-service 现状 | `backend/warehouse-service/src/main/java/com/bestwo/dataplatform/warehouse/controller/WarehouseController.java`、`backend/warehouse-service/src/main/java/com/bestwo/dataplatform/warehouse/service/DorisQueryService.java` | 已有 Doris 查询能力，能查订单明细、日汇总；并且已经识别 `ods_wx_order`、`dwd_wx_order_detail`、`ads_order_day_summary` 这些数仓表名。 | `warehouse-service` 很适合继续承接 ODS/DWD/DWS/ADS 查询、同步任务、治理最小闭环。 |
| warehouse 数据源能力 | `backend/warehouse-service/src/main/resources/application.yml`、`backend/warehouse-service/src/main/java/com/bestwo/dataplatform/warehouse/config/DorisDataSourceConfig.java` | 已接 Doris 数据源，当前查询侧走 MyBatis + Doris。 | 数仓物理层优先继续用 Doris，避免首期再引入新栈。 |
| 订单/支付业务现状 | `backend/order-service/pom.xml`、`backend/order-service/src/main/resources/application.yml` | `order-service` 真实存在，但目前只有启动类、健康检查和最小配置，没有订单、支付、微信回调代码。 | 写侧业务最适合落在 `order-service`，但需要从 0 补真实业务。 |
| sync/task/auth 现状 | `README.md` | `auth-service`、`task-service`、`sync-service` 在 README 里都明确还是骨架；扫描也未发现同步任务、元数据、质量、血缘、告警实现。 | 第一阶段不要急着把闭环拆散到这 3 个空服务里。 |
| 前端现状 | `frontend/src/router/index.ts`、`frontend/src/views/order/OrderListView.vue`、`frontend/src/views/task/TaskListView.vue`、`frontend/src/views/warehouse/WarehouseView.vue` | 已有 `/dashboard`、`/orders`、`/tasks`、`/warehouse/analysis`、`/warehouse/debug`，但 `orders`、`tasks`、`warehouse` 主页仍主要是占位。 | 前端入口够用，首期优先复用现有菜单，不必大改导航结构。 |
| 数据库能力 | `backend/README.md`、`backend/pom.xml` | 仓库明确提到 Doris 和 PostgreSQL；父 POM 同时管理 PostgreSQL、MySQL 驱动。未发现 Redis 配置，也未发现业务库 DDL/迁移脚本。 | 订单业务库建议首期明确选一个 OLTP 库，优先 PostgreSQL；数仓继续 Doris。 |
| 支付/外部接口现状 | 仓库全文扫描结果 | 未发现微信支付 SDK、商户配置、支付回调、退款、对账代码。 | 微信支付接入必须新增，但范围要收敛。 |

### 结论

- “微信支付数仓 MVP”最适合落在 `order-service + warehouse-service + gateway-service` 这条链上。
- 不建议首期把核心闭环都塞进 `warehouse-service`。
- 也不建议首期把同步、任务、认证再拆到 `sync-service`、`task-service`、`auth-service`，因为这 3 个服务当前都还是空骨架。
- 最稳妥的落地方式是：
- `order-service` 负责订单与支付写侧。
- `warehouse-service` 负责数仓接入、建模、治理、查询消费。
- `gateway-service` 继续做统一入口和鉴权。

## 2. MVP 边界定义

| 范围 | 本次 MVP 做什么 | 本次 MVP 不做什么 |
|---|---|---|
| 业务侧 | 单一订单域、单商户、账号后台下单、微信支付发起、支付回调、订单/支付状态流转 | 多商户、多支付渠道、复杂商品中心、复杂售后 |
| 支付侧 | 优先做微信 `Native/扫码支付`，最容易在后台场景闭环 | 小程序 `JSAPI`、公众号授权、复杂分账 |
| 数仓侧 | 业务库到 ODS，ODS 到 DWD，DWD 到 DWS/ADS，先做离线/准实时批处理 | Spark/Flink、实时 CDC、Airflow、湖仓全家桶 |
| 治理侧 | 元数据登记、任务日志、质量规则、最小血缘、最小告警 | 完整数据目录、审批流、统一主数据平台 |
| 前端侧 | 复用现有 `/orders`、`/tasks`、`/dashboard`、`/warehouse/analysis` | 大规模重做 UI、复杂 BI 设计器 |
| 部署侧 | 继续沿用现有 gateway / k8s / Jenkins 链路 | 新增复杂中间件体系 |

### 边界建议

- 这次 MVP 只做“真实支付订单域”。
- 订单域里最小闭环建议选“创建订单 -> 扫码支付 -> 回调成功 -> 入仓 -> 看板消费”。
- 退款、对账、风控、全量调度平台、数据目录平台都放到下一阶段。

## 3. 业务闭环拆解

| 环节 | 目标 | 最小实现内容 | 交付结果 |
|---|---|---|---|
| 下单 | 产生真实业务订单 | 后台创建订单，生成 `order_no`、金额、状态 `CREATED/WAIT_PAY` | 业务库有订单主表记录，可查询 |
| 微信支付下单 | 让订单具备支付能力 | `order-service` 调微信支付 Native 下单，拿到 `code_url` 或预下单结果 | 前端能展示支付二维码 |
| 支付回调 | 接收微信异步结果 | 新增支付回调接口，验签、记录原始回调、更新支付状态 | 支付成功后业务库状态变化 |
| 订单/支付状态流转 | 保证状态一致 | 建订单状态、支付单状态、幂等更新规则 | 订单与支付单状态可追踪 |
| 业务数据沉淀 | 形成业务事实源 | 订单表、支付单表、回调日志表落在业务库 | 有可同步的源数据 |
| 数仓采集 | 进入 ODS | 用 Java 任务把业务库数据同步到 Doris ODS 表 | ODS 有订单和支付原始层数据 |
| 数仓建模 | 支撑分析消费 | 产出 `dwd_wx_order_detail`、`dws` 汇总、`ads_order_day_summary` | `warehouse-service` 可直接复用已有查询模式 |
| 治理 | 保证数据可用 | 建最小元数据、任务日志、质量规则、血缘关系、告警记录 | 任务失败和质量异常可见 |
| 服务消费 | 页面/API 能消费成果 | `warehouse-service` 提供看板接口，前端复用 `/dashboard`、`/warehouse/analysis` | 形成“支付业务 + 数仓分析”闭环 |

## 4. 模块拆解表

| 模块 | 模块目标 | 建议落点 | 核心表 | 核心接口 | 前端页面 | 前置条件 | 优先级 |
|---|---|---|---|---|---|---|---|
| 订单与支付业务模块 | 管理订单、支付单、状态流转 | `backend/order-service` | `biz_order`、`biz_payment_order`、`biz_payment_notify_log` | 创建订单、订单列表、订单详情、支付单详情 | 复用 `/orders` | 选定业务库 | P0 |
| 微信支付接入模块 | 发起微信支付并处理回调 | `backend/order-service` | `biz_payment_order`、`biz_payment_notify_log` | 预下单、回调、主动查单 | `/orders` 支付弹窗/详情 | 商户号、证书、回调地址待确认 | P0 |
| 数据源与表接入模块 | 让 warehouse 知道源表和目标表 | `backend/warehouse-service` | `dw_meta_datasource`、`dw_meta_table`、`dw_meta_column` | 数据源登记、表登记、字段映射查看 | 复用 `/warehouse` | ODS/DWD/ADS 表设计确定 | P1 |
| 同步任务模块 | 把业务库数据拉到 ODS | `backend/warehouse-service` 首期内置 | `dw_sync_job`、`dw_sync_job_log` | 手动执行同步、查看任务结果 | 复用 `/tasks` | 业务库表已落地 | P0 |
| 数仓分层加工模块 | 从 ODS 产出 DWD/DWS/ADS | `backend/warehouse-service` | `ods_wx_order`、`ods_wx_payment_order`、`ods_wx_payment_notify`、`dwd_wx_order_detail`、`dws_wx_pay_trade_day`、`ads_order_day_summary` | 加工任务执行、汇总查询 | `/warehouse/analysis`、`/dashboard` | ODS 数据可用 | P0 |
| 数据治理模块 | 做最小质量与血缘 | `backend/warehouse-service` | `dw_quality_rule`、`dw_quality_result`、`dw_lineage_relation`、`dw_alert_record` | 质量检查、结果查询、异常列表 | `/tasks` 或 `/warehouse` | 同步/加工任务已跑通 | P1 |
| 数据消费模块 | 提供分析接口 | `backend/warehouse-service` | 主要消费 ADS/DWS | 支付概览、支付趋势、订单明细 | `/dashboard`、`/warehouse/analysis` | ADS/DWS 可用 | P0 |
| 前端展示模块 | 展示订单、支付、任务、报表 | `frontend` | 无 | 调订单、支付、数仓接口 | 现有 `/orders`、`/tasks`、`/dashboard`、`/warehouse/analysis` | 后端接口稳定 | P0 |

## 5. 数据分层设计建议

**建议优先沿用 `warehouse-service` 已经识别的表名。**  
原因是 `backend/warehouse-service/src/main/java/com/bestwo/dataplatform/warehouse/service/DorisQueryService.java` 已经直接识别 `ods_wx_order`、`dwd_wx_order_detail`、`ads_order_day_summary`。

| 层级 | 建议表 | 作用 | 来源 | 产出 | MVP 第一阶段 |
|---|---|---|---|---|---|
| ODS | `ods_wx_order` | 保留业务订单原始快照 | `biz_order` | 订单原始层 | 是 |
| ODS | `ods_wx_payment_order` | 保留支付单原始快照 | `biz_payment_order` | 支付原始层 | 是 |
| ODS | `ods_wx_payment_notify` | 保留微信回调原始报文 | `biz_payment_notify_log` | 回调原始层 | 是 |
| DWD | `dwd_wx_order_detail` | 订单与支付统一明细事实表 | ODS 订单 + ODS 支付 + ODS 回调 | 订单支付明细 | 是 |
| DWD | `dwd_wx_payment_event` | 支付事件明细 | ODS 支付 + ODS 回调 | 支付事件事实 | 否，可并到 `dwd_wx_order_detail` 后做 P1 |
| DWS | `dws_wx_pay_trade_day` | 日粒度支付汇总 | `dwd_wx_order_detail` | 日指标聚合 | 是 |
| DWS | `dws_wx_pay_status_day` | 状态分布汇总 | `dwd_wx_order_detail` | 状态分析 | P1 |
| ADS | `ads_order_day_summary` | 面向页面/API 的日汇总结果 | DWS 或 DWD | 现有汇总页可直接消费 | 是 |
| ADS | `ads_pay_dashboard_overview` | 面向 dashboard 的概览指标 | DWS | 今日支付额、成功单量、成功率 | 是 |
| ADS | `ads_pay_order_detail` | 面向订单查询页的宽表 | DWD | 订单明细列表 | P1 |

## 6. 数据治理最小闭环

| 能力 | MVP 最小需要做什么 | 建议实现方式 | 是否必做 |
|---|---|---|---|
| 元数据管理 | 至少登记数据源、表、字段、层级、负责人 | `warehouse-service` 内建元数据表 + 简单查询接口 | 必做 |
| 数据质量规则 | 至少做 4 类规则：订单主键重复、支付成功但无支付时间、回调成功但无业务订单、ADS 金额异常为负 | 规则配置表 + 任务执行后写结果表 | 必做 |
| 任务日志 | 同步任务、加工任务、质量任务都要有运行日志、开始结束时间、状态、行数、错误信息 | `dw_sync_job_log` / `dw_etl_job_log` 合并或一张总表 | 必做 |
| 血缘 | 至少能看出 `biz_order -> ods_wx_order -> dwd_wx_order_detail -> ads_order_day_summary` | 先用关系表固化，不做自动解析 | 必做 |
| 告警 | 先支持“任务失败/质量失败”记录落库并在页面标红 | 页面告警列表先行，Webhook 延后 | 必做 |
| 外部通知 | 钉钉/企业微信/短信 | 第二阶段再做 | 可延期 |

## 7. 详细开发任务拆解表

| 任务编号 | 任务名称 | 所属模块 | 目标说明 | 后端工作 | 前端工作 | 数据库工作 | 接口工作 | 依赖项 | 优先级 | 预计交付成果 |
|---|---|---|---|---|---|---|---|---|---|---|
| T01 | 设计订单/支付状态模型 | 订单与支付业务 | 明确状态流转，避免后面返工 | 新增状态枚举、状态机规则 | 无 | 无 | 无 | 无 | P0 | 状态图与代码枚举 |
| T02 | 建业务库订单主表 | 订单与支付业务 | 承载真实下单 | `order-service` 引入持久层 | 建下单表单字段 | 建 `biz_order` | `POST /api/orders` | T01 | P0 | 可写入订单 |
| T03 | 建支付单与回调日志表 | 微信支付接入 | 承载支付链路 | 建支付实体、回调日志实体 | 无 | 建 `biz_payment_order`、`biz_payment_notify_log` | 无 | T01 | P0 | 有支付落库表 |
| T04 | 接入业务库访问能力 | 订单与支付业务 | 让 `order-service` 真正可写库 | 引入 JDBC/MyBatis-Plus、Mapper、配置 | 无 | 配置业务库连接 | 无 | T02-T03 | P0 | `order-service` 可持久化 |
| T05 | 新增创建订单接口 | 订单与支付业务 | 形成业务起点 | 实现创建订单、生成 `order_no` | `/orders` 增加创建订单入口 | 使用 `biz_order` | `POST /api/orders` | T04 | P0 | 订单创建成功 |
| T06 | 新增订单列表/详情接口 | 订单与支付业务 | 支撑后台查询 | 实现分页查询和详情 | `/orders` 展示订单列表 | 查询索引 | `GET /api/orders`、`GET /api/orders/{id}` | T05 | P0 | 可查看订单 |
| T07 | 封装微信支付客户端 | 微信支付接入 | 统一对接微信 API | 封装商户配置、签名、下单、查单 | 无 | 支付配置表可先不做 | 内部服务接口 | 商户参数待确认 | P0 | 可调用微信下单 |
| T08 | 新增微信预下单接口 | 微信支付接入 | 给订单生成支付能力 | 根据订单生成 Native 支付单和二维码链接 | `/orders` 展示“去支付/刷新支付码” | 写 `biz_payment_order` | `POST /api/orders/{id}/pay` | T05、T07 | P0 | 返回 `code_url` |
| T09 | 新增支付回调接口 | 微信支付接入 | 接收异步结果 | 新增回调接口、验签、记录原始报文 | 无 | 写 `biz_payment_notify_log` | `POST /api/pay/wechat/notify` | T07 | P0 | 回调可入库 |
| T10 | 实现支付幂等与状态流转 | 订单与支付业务 | 避免重复回调污染 | 按支付单号/回调幂等更新订单和支付单状态 | `/orders` 状态实时刷新 | 状态字段、更新时间字段 | 回调内部逻辑 | T09 | P0 | 支付成功后状态正确 |
| T11 | gateway 增加 order/pay 路由与白名单 | 网关接入 | 让新业务能走 `/api` | 增加 `/api/orders/**`、`/api/pay/**` 路由；放行微信回调 | 无 | 无 | 网关转发 | T05-T10 | P0 | 新接口可通过 gateway 访问 |
| T12 | 设计并创建 ODS 表 | 数仓分层加工 | 落业务原始数据 | 在 `warehouse-service` 增加 ODS 表适配 | 无 | 建 `ods_wx_order`、`ods_wx_payment_order`、`ods_wx_payment_notify` | 无 | T05-T10 | P0 | ODS 结构稳定 |
| T13 | 实现业务库到 ODS 手动同步任务 | 同步任务 | 让业务数据进入数仓 | 新增同步服务、手动执行入口、日志记录 | `/tasks` 增加“执行同步”与日志列表 | 建任务表/日志表 | `POST /api/dw/jobs/sync-order/run` | T12 | P0 | 能从业务库同步到 ODS |
| T14 | 实现 ODS 到 DWD 加工任务 | 数仓分层加工 | 形成统一订单支付明细 | 新增 DWD SQL/Java 加工逻辑 | 无 | 建 `dwd_wx_order_detail` | `POST /api/dw/jobs/build-dwd/run` | T13 | P0 | DWD 可产出 |
| T15 | 实现 DWS/ADS 聚合任务 | 数仓分层加工 | 形成可消费指标 | 生成日汇总、概览指标 | `/dashboard` 接口对接 | 建 `dws_wx_pay_trade_day`、`ads_order_day_summary`、`ads_pay_dashboard_overview` | `POST /api/dw/jobs/build-ads/run` | T14 | P0 | ADS 可查询 |
| T16 | 扩展 warehouse-service 查询接口 | 数据消费 | 支撑页面消费 | 新增支付概览、支付趋势、任务日志、质量结果接口 | 对接 `/dashboard`、`/warehouse/analysis` | 查询 ADS/DWS | `GET /api/warehouse/pay/overview` 等 | T15 | P0 | 页面可消费数仓结果 |
| T17 | 新增元数据登记与任务日志接口 | 数据治理 | 让数据资产和任务可追踪 | 元数据表、任务日志表、查询接口 | `/tasks` 和 `/warehouse` 展示表清单/任务日志 | 建 `dw_meta_*`、`dw_job_log` | 元数据/日志查询接口 | T13-T15 | P1 | 治理基础可见 |
| T18 | 新增质量规则与质量检查任务 | 数据治理 | 防止“跑通但不可信” | 质量规则配置、执行、结果落库 | `/tasks` 展示质量结果 | 建 `dw_quality_rule`、`dw_quality_result` | 运行质量检查、查询结果 | T14-T17 | P1 | 可看到质量异常 |
| T19 | 新增最小血缘与告警记录 | 数据治理 | 让问题可追踪 | 固化表级血缘、失败告警记录 | `/warehouse` 或 `/tasks` 展示血缘/异常 | 建 `dw_lineage_relation`、`dw_alert_record` | 血缘/告警查询接口 | T17-T18 | P1 | 有最小治理闭环 |
| T20 | 改造前端订单与数仓页面 | 前端展示 | 打通用户可视闭环 | 无 | `/orders` 做下单与支付；`/dashboard` 展示支付概览；`/tasks` 展示任务日志；`/warehouse/analysis` 展示支付分析 | 无 | 调用现有与新增 API | T05-T19 | P0 | 页面可演示完整闭环 |

## 8. 推荐开发顺序

| 周次 | 建议工作 | 为什么这样排 |
|---|---|---|
| 第 1 周 | 完成 T01-T06，先把 `order-service` 的订单写侧和查询侧打通 | 没有业务主表，后续支付、数仓都无源头 |
| 第 2 周 | 完成 T07-T11，把微信支付下单、回调、gateway 路由打通 | 这是“真实业务闭环”的核心，不宜拖后 |
| 第 3 周 | 完成 T12-T16，把业务库 -> ODS -> DWD -> ADS 跑通，并接到现有 `warehouse-service` 页面/API | 这周形成“数仓闭环”，能开始展示业务价值 |
| 第 4 周 | 完成 T17-T20，补元数据、任务日志、质量规则、最小血缘与页面整合 | 这周做“可运营、可解释、可验收”，防止只是半成品 |

## 9. 验收标准

| 验收项 | 可验证标准 |
|---|---|
| 创建订单 | 通过 `/api/orders` 成功创建订单，业务库有 `biz_order` 记录 |
| 发起微信支付 | 某个订单能成功调用微信预下单，拿到 `code_url` 或等价支付参数 |
| 处理支付回调 | 微信回调或等价回调测试后，订单状态变为已支付，支付单状态同步更新 |
| 业务数据沉淀 | 订单、支付单、回调日志都能在业务库查询到 |
| ODS 同步 | 手动执行同步任务后，ODS 三张表有新增数据，任务日志记录成功/失败 |
| DWD/ADS 产出 | `dwd_wx_order_detail`、`ads_order_day_summary` 有正确数据 |
| 数仓接口消费 | `warehouse-service` 的订单明细、日汇总、支付概览接口可返回数据 |
| 页面消费 | `/orders` 可看订单与支付状态，`/dashboard` 可看支付概览，`/warehouse/analysis` 可查分析结果 |
| 数据质量 | 至少 1 条质量规则能跑出结果，并可在页面/API 查看 |
| 任务可追踪 | 至少能看到同步任务、加工任务、质量任务的运行日志 |

## 10. 风险与建议

- 最大风险不是代码，而是微信支付商户参数。仓库里未发现商户号、证书、APIv3 密钥、回调地址配置，这部分如果不先确认，支付链路会卡住。
- 不要首期就把 `sync-service`、`task-service`、`auth-service` 一起做实。当前最小闭环更适合“`order-service` 写侧 + `warehouse-service` 数仓侧 + `gateway-service` 路由鉴权”。
- 不要首期上 Spark、Flink、Airflow、CDC。当前仓库已经有 Java 服务和 Doris，首期用 Java 任务就够了。
- 不要首期把退款、对账、分账、风控一起做。对 MVP 来说，支付成功闭环比功能全更重要。
- 表名最好优先对齐 `backend/warehouse-service/src/main/java/com/bestwo/dataplatform/warehouse/service/DorisQueryService.java` 里已经识别的 `ods_wx_order`、`dwd_wx_order_detail`、`ads_order_day_summary`，这样能显著减少改造量。
- 业务库选型需要尽快拍板。仓库有 PostgreSQL 线索，也有 MySQL 驱动管理，但未发现 `order-service` 真实库配置。建议首期固定一个 OLTP 库，优先 PostgreSQL；若现网已有 MySQL 业务库，则以现网为准。
- 前端不要新增太多新菜单。复用现有 `/orders`、`/tasks`、`/dashboard`、`/warehouse/analysis`，最容易快速上线。
- 这次 MVP 的成功标准不是“做了多少表”，而是“至少跑通 1 笔真实订单支付，并能在数仓页面看到对应统计结果”。
