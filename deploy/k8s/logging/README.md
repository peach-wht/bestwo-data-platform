# k8s 集群日志栈

## 方案概览

这套日志栈面向当前仓库的多服务 Spring Boot + k3s 场景，链路如下：

1. 应用输出统一 JSON 日志到 stdout / rolling file
2. `Grafana Alloy` 以 DaemonSet 方式采集每个节点上的 Pod 日志
3. `Loki` 负责集中存储与查询
4. `Grafana` 负责检索、筛选和排障看板

这次选择 `Alloy` 而不是 `Promtail`，是因为 `Promtail` 已于 **2026-03-02** 进入 EOL，不适合作为新系统的采集器。

## 版本

当前清单固定了以下版本：

- Loki: `3.7.1`
- Alloy: `1.12.1`
- Grafana: `12.3.6`

## 部署顺序

```bash
kubectl apply -f deploy/k8s/logging/namespace.yaml
kubectl apply -f deploy/k8s/logging/loki-configmap.yaml
kubectl apply -f deploy/k8s/logging/loki-service.yaml
kubectl apply -f deploy/k8s/logging/loki-statefulset.yaml
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
kubectl -n observability rollout status statefulset/loki
kubectl -n observability rollout status daemonset/alloy-logs
kubectl -n observability rollout status deployment/grafana
kubectl -n observability logs daemonset/alloy-logs
kubectl -n observability logs statefulset/loki
```

## Grafana 接入

`grafana-datasource-configmap.yaml` 已经预置 Loki 数据源，Grafana 启动后可直接查询。

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

1. Loki 从单实例 PVC 切到对象存储后端
2. 把 Loki 拆成读写后端模式
3. 增加日志告警规则和派生指标
4. 接入 Tempo / Mimir，形成完整 LGTM 栈
