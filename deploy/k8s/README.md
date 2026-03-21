# warehouse-service deployment sample

## Jenkins credentials

- `ACR_USERNAME_PASSWORD`
  - Type: `Username with password`
  - Purpose: `docker login` to Alibaba Cloud ACR
- `K3S_KUBECONFIG`
  - Type: `Secret file`
  - Purpose: provide kubeconfig to `kubectl` in Jenkins Pipeline

## First deployment

1. Create the `app` namespace if it does not exist:

```bash
kubectl create namespace app --dry-run=client -o yaml | kubectl apply -f -
```

2. Create the business secret:

```bash
kubectl apply -f deploy/k8s/warehouse-service-secret-example.yaml
```

Before applying, replace `CHANGE_ME` in `warehouse-service-secret-example.yaml`, or create the secret directly:

```bash
kubectl -n app create secret generic warehouse-service-secret \
  --from-literal=doris-password='CHANGE_ME' \
  --dry-run=client -o yaml | kubectl apply -f -
```

3. Create the ACR image pull secret:

```bash
kubectl -n app create secret docker-registry acr-registry-secret \
  --docker-server=registry.cn-hangzhou.aliyuncs.com \
  --docker-username=YOUR_ACR_USERNAME \
  --docker-password=YOUR_ACR_PASSWORD \
  --docker-email=your-email@example.com
```

4. Apply the Service:

```bash
kubectl apply -f deploy/k8s/warehouse-service-service.yaml
```

5. Apply the Deployment:

```bash
kubectl apply -f deploy/k8s/warehouse-service-deployment.yaml
```

6. Set the first image manually:

```bash
kubectl -n app set image deployment/warehouse-service \
  warehouse-service=registry.cn-hangzhou.aliyuncs.com/bestwo/warehouse-service:latest
```

## Jenkins release flow

- Use the repository root `Jenkinsfile`
- Trigger on `main` push, or run the Pipeline manually
- The Pipeline will:
  - checkout code
  - print branch and commit
  - build `warehouse-service` with Maven
  - build the Docker image
  - login to Alibaba Cloud ACR
  - push the image
  - update the Deployment with `kubectl set image`
  - wait for `kubectl rollout status`

Default image example:

```text
registry.cn-hangzhou.aliyuncs.com/bestwo/warehouse-service:${IMAGE_TAG}
```

You can change these Jenkins parameters when needed:

- `REGISTRY`
- `IMAGE_NAMESPACE`
- `IMAGE_NAME`
- `IMAGE_REPO`
- `IMAGE_TAG`

## Verification

```bash
kubectl -n app get pods
kubectl -n app get pods -l app.kubernetes.io/name=warehouse-service -o wide
kubectl -n app rollout status deployment/warehouse-service
kubectl -n app get deployment warehouse-service -o yaml
```

## Troubleshooting

```bash
kubectl -n app get pods
kubectl -n app describe pod <pod-name>
kubectl -n app logs deploy/warehouse-service
kubectl -n app get secret
kubectl -n app rollout status deployment/warehouse-service
kubectl -n app get deployment warehouse-service -o yaml
```

## Common issues

- `ImagePullBackOff`
  - Check `acr-registry-secret`, the ACR registry address, and the image tag
- rollout failure
  - Check `/health`, `warehouse-service-secret`, `DORIS_PASSWORD`, and the application startup logs
- Jenkins push failure
  - Check the `ACR_USERNAME_PASSWORD` credential in Jenkins
