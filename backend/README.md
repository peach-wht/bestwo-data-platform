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

- `spring.datasource.host=127.0.0.1`
- `spring.datasource.port=5432`
