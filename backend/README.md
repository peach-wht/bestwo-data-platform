# backend

## Local SSH Tunnels

For local development, some backend data sources are accessed through SSH tunnels.

### Doris

PowerShell:

```powershell
ssh -i "$HOME\.ssh\my-ecs.pem" -N -L 9030:172.31.246.223:9030 root@8.130.188.79
```

Details:

- Local port: `9030`
- Remote target: `172.31.246.223:9030`
- Jump server: `root@8.130.188.79`
- Key: `$HOME\.ssh\my-ecs.pem`

Typical local config:

- `doris.host=127.0.0.1`
- `doris.port=9030`

### PostgreSQL

PowerShell:

```powershell
ssh -i "D:\key\my-ecs.pem" -N -L 5432:127.0.0.1:5432 root@8.130.159.125
```

Details:

- Local port: `5432`
- Remote target: `127.0.0.1:5432`
- Jump server: `root@8.130.159.125`
- Key: `D:\key\my-ecs.pem`

Typical local config:

- `ORDER_DB_HOST=127.0.0.1`
- `ORDER_DB_PORT=5432`
- `ORDER_DB_NAME=bestwo_app`
- `ORDER_DB_USERNAME=bestwo`
- `ORDER_DB_PASSWORD=Bestwo@123`

Typical k8s config:

- `ORDER_DB_HOST=postgres.infra.svc.cluster.local`
- `ORDER_DB_PORT=5432`
- `ORDER_DB_NAME=bestwo_app`
- `ORDER_DB_USERNAME=bestwo`
- `ORDER_DB_PASSWORD=Bestwo@123`
