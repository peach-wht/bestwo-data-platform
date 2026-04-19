# 分布式集群日志系统设计

## 目标

当前这套日志系统的目标不是“把控制台输出改漂亮”，而是把后端多服务项目升级为可在 k8s 集群中稳定使用的可观测日志体系：

- 所有服务统一输出结构化 JSON 日志
- 网关和服务之间透传 `X-Trace-Id` / `X-Request-Id`
- Servlet / WebFlux 统一接入访问日志
- 订单、预支付、支付回调、数仓任务这些关键链路补齐业务事件日志
- 集群侧用 Grafana Alloy 采集，用 Loki 存储，用 Grafana 查询

## 已落地内容

### 1. 应用侧统一日志基础设施

公共模块位置：

- `backend/common/src/main/java/com/bestwo/dataplatform/common/logging`
- `backend/common/src/main/resources/bestwo-logback-spring.xml`

核心能力：

- `BestwoLoggingAutoConfiguration`
  - Spring Boot 3 自动装配
  - 对 Servlet / WebFlux 服务自动注册访问日志过滤器
  - 对 `RestClient` 自动注入链路头透传拦截器
- `ServletTraceLoggingFilter`
  - 为 MVC 服务生成 / 接收链路 ID
  - 注入 MDC
  - 记录统一访问日志
- `ReactiveTraceLoggingWebFilter`
  - 为 Gateway/WebFlux 服务生成 / 接收链路 ID
  - 将链路头透传到下游
  - 记录统一访问日志
- `CurrentTraceClientHttpRequestInterceptor`
  - 让服务发出的 `RestClient` 请求自动带上 `X-Trace-Id` / `X-Request-Id`

默认透传头：

- `X-Trace-Id`
- `X-Request-Id`
- `X-User-Id`
- `X-Client-Ip`
- `X-Source-Service`

### 2. 统一 JSON Logback

所有服务的 `application.yml` 已切到：

```yaml
logging:
  config: classpath:bestwo-logback-spring.xml
```

日志输出策略：

- 控制台 JSON 输出，适配 k8s stdout 采集
- 本地滚动文件输出，适配单机排障
- `AsyncAppender` 异步写入，降低业务线程阻塞

统一字段：

- `service`
- `cluster`
- `environment`
- `instance`
- `traceId`
- `requestId`
- `httpMethod`
- `httpPath`
- `clientIp`
- `level`
- `logger_name`
- `message`

### 3. 关键业务日志

已经补齐的关键事件：

- 订单创建：`order_created`
- 预支付复用 / 发起 / 成功 / 失败
- 微信支付回调接收 / 处理成功 / 处理失败
- 数仓同步任务开始 / 批次进度 / 完成 / 失败
- ADS 构建开始 / 完成 / 失败
- 全局异常出口统一打点

## 集群侧架构

日志链路如下：

1. `gateway-service` 接收请求并生成 `traceId`
2. 下游 `order-service` / `warehouse-service` 继承同一个 `traceId`
3. 各服务将访问日志和业务日志输出为 JSON
4. `Grafana Alloy` 以 DaemonSet 方式从 Kubernetes API 采集 Pod 日志
5. `Loki` 负责存储和索引
6. `Grafana` 作为查询与排障入口

部署清单位置：

- `deploy/k8s/logging`

## 推荐查询方式

### 按 traceId 查一条完整链路

```logql
{namespace="app"} | json | traceId="YOUR_TRACE_ID"
```

### 查支付回调失败

```logql
{service="order-service"} | json | event="wechat_pay_notify_failed"
```

### 查慢请求

```logql
{service="gateway-service"} | json | event="http_access" | durationMs > 1000
```

### 查数仓任务执行

```logql
{service="warehouse-service"} | json | jobCode="sync-order-to-ods"
```

## 目前这版的边界

这次交付的是一套可以直接跑起来的集群日志基线，重点先把“统一产生日志”和“统一收集检索”做扎实。

当前 `deploy/k8s/logging` 里的 Loki 采用单实例 + PVC，更适合当前项目规模和 k3s 场景。后续如果你要走更强的高可用，可以继续升级为：

- Loki 分布式读写后端模式
- 对象存储切到 OSS / S3
- 加上告警规则与日志派生指标
- 再把 Trace/Metric 接到完整 LGTM 栈
