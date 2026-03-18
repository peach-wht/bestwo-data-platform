# bestwo-data-platform

面向前后端分离架构的数据平台基础项目。

## Backend

当前后端采用分布式多模块设计，围绕“微信订单数据接入与数仓分析平台”进行拆分，便于后续部署到 k3s，并逐步接入 PostgreSQL、Doris 与 OSS。

backend/ 模块说明：

- `common`：公共返回体、异常、枚举与基础工具类
- `gateway-service`：统一网关入口，负责后续服务路由与流量接入
- `auth-service`：认证鉴权服务骨架，当前仅保留最小可运行结构
- `order-service`：微信订单接入服务骨架，后续承接订单采集与处理
- `warehouse-service`：数仓分析服务骨架，后续用于 Doris 查询与聚合分析
- `task-service`：任务调度服务骨架，后续承接平台内部任务编排
- `sync-service`：外部数据同步服务骨架，后续用于 ERP 等系统回流同步

当前阶段仅完成基础骨架，包括 Maven 父子模块、Spring Boot 启动类、最小配置文件、健康检查接口与少量公共类，暂未引入复杂业务实现。

## Frontend

frontend/ 是管理后台前端工程，用于承载“微信订单数据接入与数仓分析平台”的后台操作界面。

当前技术栈为 Vue 3 + Vite + TypeScript + Element Plus，并集成 Vue Router、Pinia 与 Axios 作为基础能力。

当前阶段仅完成前端基础骨架和页面占位，包括登录页、仪表盘、订单管理、任务管理、数仓管理、404 页面，以及基础布局、路由、状态管理和 API 封装，暂未引入复杂业务逻辑。
