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
