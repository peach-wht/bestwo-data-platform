# k8s 集群日志栈

## 方案概览

这套日志栈面向当前仓库的多服务 Spring Boot + k3s 场景，链路如下：

1. 应用输出统一 JSON 日志到 stdout / rolling file
2. `Grafana Alloy` 以 DaemonSet 方式采集每个节点上的 Pod 日志
3. 现有 `Loki` 负责集中存储与查询
4. `Grafana` 负责检索、筛选和排障看板

这次选择 `Alloy` 而不是 `Promtail`，是因为 `Promtail` 已于 **2026-03-02** 进入 EOL，不适合作为新系统的采集器。

## 版本

当前清单固定了以下组件版本：

- Alloy: `1.12.1`
- Grafana: `12.3.6`

如果你线上已经有 Loki，这份仓库清单默认走“接入现有 Loki”模式，不要求重复部署 Loki。

## 先配置现有 Loki 地址

先复制并修改：

```bash
cp deploy/k8s/logging/logging-stack-configmap.example.yaml deploy/k8s/logging/logging-stack-configmap.yaml
```

需要填两项：

- `loki-push-url`
  - 给 Alloy 写日志用
  - 例如 `http://loki-gateway.observability.svc.cluster.local/loki/api/v1/push`
- `loki-query-url`
  - 给 Grafana 查日志用
  - 例如 `http://loki-gateway.observability.svc.cluster.local`

## 部署顺序

```bash
kubectl apply -f deploy/k8s/logging/namespace.yaml
kubectl apply -f deploy/k8s/logging/logging-stack-configmap.yaml
kubectl apply -f deploy/k8s/logging/alloy-rbac.yaml
kubectl apply -f deploy/k8s/logging/alloy-configmap.yaml
kubectl apply -f deploy/k8s/logging/alloy-daemonset.yaml
kubectl apply -f deploy/k8s/logging/grafana-secret-example.yaml
kubectl apply -f deploy/k8s/logging/grafana-datasource-configmap.yaml
kubectl apply -f deploy/k8s/logging/grafana-pvc.yaml
kubectl apply -f deploy/k8s/logging/grafana-deployment.yaml
kubectl apply -f deploy/k8s/logging/grafana-service.yaml
```

## 验证

```bash
kubectl -n observability get pods
kubectl -n observability rollout status daemonset/alloy-logs
kubectl -n observability rollout status deployment/grafana
kubectl -n observability logs daemonset/alloy-logs
```

## Grafana 接入

`grafana-datasource-configmap.yaml` 已经预置 Loki 数据源，Grafana 会从环境变量 `LOKI_DATASOURCE_URL` 读取你配置的 Loki 查询地址。

如果你需要从集群外访问：

- 临时方式：`kubectl -n observability port-forward svc/grafana 3000:3000`
- 正式方式：单独补一份 Ingress

## 推荐查询

按 traceId 查整条链路：

```logql
{namespace="app"} | json | traceId="YOUR_TRACE_ID"
```

查支付失败：

```logql
{service="order-service"} | json | event="payment_prepay_failed"
```

查慢请求：

```logql
{service="gateway-service"} | json | event="http_access" | durationMs > 1000
```

## 下一步增强

如果你准备把这套系统升级到更强的生产态，建议按这个顺序继续演进：

1. 给现有 Loki 增加对象存储后端和保留策略
2. 增加日志告警规则和派生指标
3. 接入 Tempo / Mimir，形成完整 LGTM 栈
4. 为核心业务补 Grafana dashboard 与 saved queries

## 可选

仓库里仍然保留了 `loki-*.yaml`，如果你后面想在独立环境快速起一套自带 Loki 的最小日志栈，可以再单独 apply 这些文件。
