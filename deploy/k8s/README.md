# warehouse-service deployment notes

## Image repository

The verified image repository for this service is:

```text
wuhaotian-acr-registry-vpc.cn-wulanchabu.cr.aliyuncs.com/bestwo/warehouse:${IMAGE_TAG}
```

## First deployment

1. Create the business secret:

```bash
kubectl apply -f deploy/k8s/warehouse-service-secret-example.yaml
```

Before applying, replace these keys with real values:

- `doris-host`
- `doris-port`
- `doris-database`
- `doris-username`
- `doris-password`
- `order-db-host`
- `order-db-port`
- `order-db-name`
- `order-db-schema`
- `order-db-username`
- `order-db-password`

2. Create the registry pull secret:

```bash
kubectl -n app create secret docker-registry acr-registry-secret \
  --docker-server=wuhaotian-acr-registry-vpc.cn-wulanchabu.cr.aliyuncs.com \
  --docker-username=YOUR_ACR_USERNAME \
  --docker-password=YOUR_ACR_PASSWORD \
  --docker-email=your-email@example.com
```

3. Apply the Service:

```bash
kubectl apply -f deploy/k8s/warehouse-service-service.yaml
```

4. Apply the Deployment if needed:

```bash
kubectl apply -f deploy/k8s/warehouse-service-deployment.yaml
```

## Local build and image build

Use the helper script from the repository root:

```bash
chmod +x scripts/build-warehouse.sh
./scripts/build-warehouse.sh
```

The script will:

1. build `warehouse-service` from the `backend` aggregator
2. copy the fat jar to `backend/warehouse-service/app.jar`
3. build the Docker image with `backend/warehouse-service/Dockerfile`

## Jenkins release flow

The root `Jenkinsfile` now matches the verified manual process:

1. Checkout
2. Build fat jar from `backend`
3. Prepare `app.jar`
4. Build Docker image
5. Push image to ACR
6. Apply `warehouse-service` Service and update Deployment image

Required Jenkins credentials:

- `REGISTRY_CREDENTIALS_ID`
  - type: Username with password
  - used for `docker login`

## Verification

```bash
kubectl -n app get svc warehouse-service
kubectl -n app get pods -l app.kubernetes.io/name=warehouse-service -o wide
kubectl -n app rollout status deployment/warehouse-service
kubectl -n app logs deploy/warehouse-service
```
