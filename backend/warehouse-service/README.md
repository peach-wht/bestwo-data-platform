# warehouse-service

## Local Doris Debug

When developing locally, Doris is reached through an SSH tunnel instead of direct access.

Start the tunnel in PowerShell:

```powershell
ssh -i "$HOME\.ssh\my-ecs.pem" -N -L 9030:172.31.246.223:9030 root@8.130.188.79
```

Tunnel details:

- Local forward port: `9030`
- Remote Doris host: `172.31.246.223`
- Remote Doris port: `9030`
- Jump server: `root@8.130.188.79`
- Private key: `$HOME\.ssh\my-ecs.pem`

After the tunnel is up, local `warehouse-service` should connect to Doris through:

- `doris.host=127.0.0.1`
- `doris.port=9030`

Typical local debug chain:

1. Start the SSH tunnel.
2. Start `warehouse-service`.
3. Start `gateway-service`.
4. Start `frontend`.
5. Access `/warehouse/ping` or `/warehouse/orders/test` through the gateway.

## ODS Schema Init

The warehouse module now keeps the Doris ODS DDL files under:

- `src/main/resources/sql/doris/ods/01_create_ods_wx_order.sql`
- `src/main/resources/sql/doris/ods/02_create_ods_wx_payment_order.sql`
- `src/main/resources/sql/doris/ods/03_create_ods_wx_payment_notify.sql`

You can initialize the three ODS tables through the service itself:

```http
POST /warehouse/schema/ods/init
```

To inspect whether the tables already exist:

```http
GET /warehouse/schema/ods
```

These ODS tables are prepared for the next MVP steps:

- `ods_wx_order`
- `ods_wx_payment_order`
- `ods_wx_payment_notify`

## Manual Sync Job

The service now supports a minimal manual sync job from PostgreSQL business tables
to Doris ODS tables.

Source PostgreSQL defaults:

- `ORDER_DB_HOST=127.0.0.1`
- `ORDER_DB_PORT=5432`
- `ORDER_DB_NAME=bestwo_app`
- `ORDER_DB_USERNAME=bestwo`
- `ORDER_DB_PASSWORD=Bestwo@123`

In k8s, override:

- `ORDER_DB_HOST=postgres.infra.svc.cluster.local`

Manual sync endpoints:

- `POST /dw/jobs/sync-order/run`
- `GET /dw/jobs/sync-order/logs`

If called through the gateway:

- `POST /api/dw/jobs/sync-order/run`
- `GET /api/dw/jobs/sync-order/logs`

## DWD Build Job

The service now also supports building the DWD wide table:

- `dwd_wx_order_detail`

Manual build endpoints:

- `POST /dw/jobs/build-dwd/run`
- `GET /dw/jobs/build-dwd/logs`

If called through the gateway:

- `POST /api/dw/jobs/build-dwd/run`
- `GET /api/dw/jobs/build-dwd/logs`

## DWS And ADS Build Job

The service now also supports building the aggregated DWS and ADS tables:

- `dws_wx_pay_trade_day`
- `ads_order_day_summary`
- `ads_pay_dashboard_overview`

Manual build endpoints:

- `POST /dw/jobs/build-ads/run`
- `GET /dw/jobs/build-ads/logs`

If called through the gateway:

- `POST /api/dw/jobs/build-ads/run`
- `GET /api/dw/jobs/build-ads/logs`

After `build-ads` completes, the existing day summary query can directly read:

- `GET /warehouse/summary/day`
- `GET /api/warehouse/summary/day`

## Warehouse Query APIs

The warehouse module now exposes minimal read APIs for dashboard and analysis pages:

- `GET /warehouse/pay/overview`
- `GET /warehouse/pay/trend`
- `GET /warehouse/jobs/logs`
- `GET /warehouse/quality/results`

Gateway paths:

- `GET /api/warehouse/pay/overview`
- `GET /api/warehouse/pay/trend`
- `GET /api/warehouse/jobs/logs`
- `GET /api/warehouse/quality/results`

Notes:

- `pay/overview` supports optional `startDate` and `endDate`. If not provided, it reads the latest overall overview.
- `pay/trend` requires `startDate` and `endDate`.
- `jobs/logs` supports optional `jobCode` and `limit`.
- `quality/results` currently returns an empty list when the quality result table has not been created yet. The real quality task will be added in the next stage.
