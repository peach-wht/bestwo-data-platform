# Jenkins + Kaniko sample for warehouse-service

## What this sample does

- Deploys Jenkins into the `infra` namespace on k3s
- Stores Jenkins data on a PVC
- Lets Jenkins call `kubectl` inside the cluster
- Uses a temporary Kaniko Pod to build and push the `warehouse-service` image
- Does not require Docker on the node

The current image target is:

```text
wuhaotian-acr-registry.cn-wulanchabu.cr.aliyuncs.com/bestwo/warehouse:${IMAGE_TAG}
```

## 1. Deploy Jenkins to k3s

Apply the manifests in this order:

```bash
kubectl apply -f deploy/k8s/jenkins/namespace.yaml
kubectl apply -f deploy/k8s/jenkins/jenkins-pvc.yaml
kubectl apply -f deploy/k8s/jenkins/jenkins-sa-rbac.yaml
kubectl apply -f deploy/k8s/jenkins/jenkins-service.yaml
kubectl apply -f deploy/k8s/jenkins/jenkins-deployment.yaml
```

## 2. Access Jenkins for the first time

This sample uses `NodePort`, so you can open:

```text
http://<app-node-ip>:30080
```

If you prefer to stay inside the cluster network, you can switch the Service to `ClusterIP` and use `kubectl port-forward`.

## 3. Get the initial admin password

```bash
kubectl -n infra exec deploy/jenkins -- cat /var/jenkins_home/secrets/initialAdminPassword
```

## 4. Minimal Jenkins plugins

Install only the minimum plugins you need:

- Pipeline
- Git
- GitHub
- Credentials Binding

If Jenkins already includes some of them, keep the existing versions.

## 5. Create the ACR secret for Kaniko

Recommended command:

```bash
kubectl -n infra create secret docker-registry acr-docker-config \
  --docker-server=wuhaotian-acr-registry.cn-wulanchabu.cr.aliyuncs.com \
  --docker-username=YOUR_ACR_USERNAME \
  --docker-password=YOUR_ACR_PASSWORD \
  --docker-email=your-email@example.com
```

You can also apply the placeholder file:

```bash
kubectl apply -f deploy/k8s/jenkins/kaniko-secret-example.yaml
```

Do not keep placeholder credentials in a real cluster.

## 6. Create the Jenkins Pipeline job

1. Open Jenkins
2. Create a new item
3. Choose `Pipeline`
4. In the Pipeline definition, choose `Pipeline script from SCM`
5. Point it to this monorepo
6. Set the script path to:

```text
Jenkinsfile
```

## 7. Bind the GitHub repository

- Public repo:
  - HTTPS checkout is enough
- Private repo:
  - add Jenkins credentials for GitHub access
  - if SSH checkout is required, you can create an optional `GITHUB_SSH_KEY`

The current sample does not force a specific GitHub credential type.

## 8. Trigger a build

- Manual trigger:
  - open the Pipeline job and click `Build with Parameters`
- Automatic trigger:
  - bind a GitHub webhook to Jenkins and trigger on `main`

The current Pipeline defaults to:

- `SERVICE_NAME=warehouse-service`
- `KANIKO_NAMESPACE=infra`
- `KANIKO_SECRET_NAME=acr-docker-config`
- `KANIKO_EXECUTOR_IMAGE=gcr.io/kaniko-project/executor:v1.23.2-debug`
- `IMAGE_REPO=wuhaotian-acr-registry.cn-wulanchabu.cr.aliyuncs.com/bestwo/warehouse`
- `DEPLOY_TO_K8S=false`

## 9. Confirm the image in ACR

After the Pipeline succeeds, check:

- Alibaba Cloud ACR console
- namespace: `bestwo`
- repository: `warehouse`
- pushed tag: the short commit SHA or the explicit `IMAGE_TAG`

## 10. Public domain vs VPC domain

- Public domain:
  - `wuhaotian-acr-registry.cn-wulanchabu.cr.aliyuncs.com`
  - easiest for the first successful push
- VPC domain:
  - `wuhaotian-acr-registry-vpc.cn-wulanchabu.cr.aliyuncs.com`
  - better if your k3s nodes have direct VPC connectivity

If you later switch to the VPC domain, update both:

- the Jenkins Pipeline image repository parameter
- the `acr-docker-config` secret content

If your cluster cannot pull `gcr.io/kaniko-project/executor`, mirror that image to your own registry first, then override:

- `KANIKO_EXECUTOR_IMAGE`

## 11. How this Pipeline works

The Pipeline does not use Docker on the node.

Instead, Jenkins:

1. checks out the monorepo
2. prepares a temporary Kaniko Pod in `infra`
3. copies the current workspace into that Pod
4. runs `/kaniko/executor`
5. pushes the image to ACR

## 12. Future extension to deployment

The root `Jenkinsfile` already reserves:

```text
DEPLOY_TO_K8S=false
```

When you are ready, turn it on and let Jenkins execute:

```bash
kubectl -n app set image deployment/warehouse-service \
  warehouse-service=wuhaotian-acr-registry.cn-wulanchabu.cr.aliyuncs.com/bestwo/warehouse:${IMAGE_TAG}
```

Then verify rollout:

```bash
kubectl -n app rollout status deployment/warehouse-service --timeout=180s
```

## 13. Useful troubleshooting commands

```bash
kubectl -n infra get pods
kubectl -n infra logs deploy/jenkins
kubectl -n infra describe pod <jenkins-pod-name>
kubectl -n infra get secret acr-docker-config -o yaml
kubectl -n infra get pod -l app.kubernetes.io/name=kaniko-builder
kubectl -n infra get pvc
kubectl -n app get deployment warehouse-service
```
