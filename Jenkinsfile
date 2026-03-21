pipeline {
    agent any

    parameters {
        string(name: 'SERVICE_NAME', defaultValue: 'warehouse-service', description: 'Service module name in the monorepo.')
        string(name: 'KANIKO_NAMESPACE', defaultValue: 'infra', description: 'Namespace where the Kaniko Pod will run.')
        string(
            name: 'KANIKO_EXECUTOR_IMAGE',
            defaultValue: 'gcr.io/kaniko-project/executor:v1.23.2-debug',
            description: 'Kaniko executor image. Override this if your cluster uses an internal mirror.'
        )
        string(name: 'KANIKO_SECRET_NAME', defaultValue: 'acr-docker-config', description: 'Kubernetes Secret name mounted into /kaniko/.docker/config.json.')
        string(
            name: 'IMAGE_REPO',
            defaultValue: 'wuhaotian-acr-registry.cn-wulanchabu.cr.aliyuncs.com/bestwo/warehouse',
            description: 'Full ACR repository path without tag.'
        )
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Optional image tag. Leave empty to use short commit SHA, then fall back to BUILD_NUMBER.'
        )
        booleanParam(
            name: 'DEPLOY_TO_K8S',
            defaultValue: false,
            description: 'Optional follow-up deployment switch. Default false only builds and pushes the image.'
        )
        string(name: 'DEPLOY_NAMESPACE', defaultValue: 'app', description: 'Kubernetes namespace for the optional deployment stage.')
        string(name: 'DEPLOYMENT_NAME', defaultValue: 'warehouse-service', description: 'Kubernetes Deployment name for the optional deployment stage.')
        string(name: 'CONTAINER_NAME', defaultValue: 'warehouse-service', description: 'Container name for kubectl set image.')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Print Environment') {
            steps {
                script {
                    env.CURRENT_BRANCH = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                    env.GIT_SHORT_SHA = env.GIT_COMMIT ? env.GIT_COMMIT.take(8) : ''
                }

                sh """
                    echo '=== SCM Context ==='
                    echo 'Branch: ${CURRENT_BRANCH}'
                    echo 'Commit: ${GIT_SHORT_SHA}'
                    echo 'Service: ${params.SERVICE_NAME}'
                    echo 'Kaniko Namespace: ${params.KANIKO_NAMESPACE}'
                """
            }
        }

        stage('Prepare IMAGE_TAG') {
            steps {
                script {
                    env.RESOLVED_IMAGE_TAG = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : (env.GIT_SHORT_SHA ?: env.BUILD_NUMBER)
                    env.FULL_IMAGE = "${params.IMAGE_REPO}:${env.RESOLVED_IMAGE_TAG}"
                    env.KANIKO_POD_NAME = "kaniko-${params.SERVICE_NAME}-${env.BUILD_NUMBER ?: '0'}"
                        .toLowerCase()
                        .replaceAll(/[^a-z0-9-]/, '-')
                        .take(50)
                        .replaceAll(/-+$/, '')
                }

                sh """
                    echo '=== Image Metadata ==='
                    echo 'Image Repo: ${params.IMAGE_REPO}'
                    echo 'Image Tag: ${RESOLVED_IMAGE_TAG}'
                    echo 'Full Image: ${FULL_IMAGE}'
                    echo 'Kaniko Pod: ${KANIKO_POD_NAME}'
                """
            }
        }

        stage('Build and Push Image with Kaniko') {
            steps {
                sh """
                    set -eu

                    export KUBECONFIG="\$WORKSPACE/.kube/config"
                    mkdir -p "\$WORKSPACE/.kube"

                    kubectl config set-cluster in-cluster \\
                      --server="https://\${KUBERNETES_SERVICE_HOST}:\${KUBERNETES_SERVICE_PORT_HTTPS}" \\
                      --certificate-authority=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt \\
                      --embed-certs=true >/dev/null

                    kubectl config set-credentials jenkins \\
                      --token="\$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" >/dev/null

                    kubectl config set-context jenkins@in-cluster \\
                      --cluster=in-cluster \\
                      --user=jenkins \\
                      --namespace=${params.KANIKO_NAMESPACE} >/dev/null

                    kubectl config use-context jenkins@in-cluster >/dev/null

                    cleanup() {
                      kubectl -n ${params.KANIKO_NAMESPACE} delete pod ${KANIKO_POD_NAME} --ignore-not-found=true >/dev/null 2>&1 || true
                    }

                    trap cleanup EXIT

                    echo '=== Prepare Build Context ==='
                    rm -rf .kaniko-context
                    mkdir -p .kaniko-context
                    cp .dockerignore .kaniko-context/.dockerignore
                    cp -r backend .kaniko-context/backend

                    echo '=== Create Kaniko Pod ==='
                    kubectl -n ${params.KANIKO_NAMESPACE} delete pod ${KANIKO_POD_NAME} --ignore-not-found=true
                    cat <<EOF | kubectl -n ${params.KANIKO_NAMESPACE} apply -f -
                    apiVersion: v1
                    kind: Pod
                    metadata:
                      name: ${KANIKO_POD_NAME}
                      labels:
                        app.kubernetes.io/name: kaniko-builder
                        app.kubernetes.io/part-of: bestwo-data-platform
                    spec:
                      serviceAccountName: jenkins
                      restartPolicy: Never
                      containers:
                        - name: kaniko
                          image: ${params.KANIKO_EXECUTOR_IMAGE}
                          imagePullPolicy: IfNotPresent
                          command:
                            - /busybox/sh
                            - -c
                            - sleep 3600
                          volumeMounts:
                            - name: workspace
                              mountPath: /workspace
                            - name: docker-config
                              mountPath: /kaniko/.docker
                      volumes:
                        - name: workspace
                          emptyDir: {}
                        - name: docker-config
                          secret:
                            secretName: ${params.KANIKO_SECRET_NAME}
                            items:
                              - key: .dockerconfigjson
                                path: config.json
                    EOF

                    echo '=== Wait For Kaniko Pod ==='
                    kubectl -n ${params.KANIKO_NAMESPACE} wait --for=condition=Ready pod/${KANIKO_POD_NAME} --timeout=180s

                    echo '=== Copy Workspace Into Kaniko Pod ==='
                    kubectl -n ${params.KANIKO_NAMESPACE} exec ${KANIKO_POD_NAME} -- mkdir -p /workspace/src
                    kubectl -n ${params.KANIKO_NAMESPACE} cp .kaniko-context/. ${KANIKO_POD_NAME}:/workspace/src

                    echo '=== Run Kaniko Build And Push ==='
                    kubectl -n ${params.KANIKO_NAMESPACE} exec ${KANIKO_POD_NAME} -- \\
                      /kaniko/executor \\
                      --context=/workspace/src \\
                      --dockerfile=/workspace/src/backend/warehouse-service/Dockerfile \\
                      --destination=${FULL_IMAGE} \\
                      --cache=false \\
                      --verbosity=info

                    echo '=== Kaniko Build Complete ==='
                    echo 'Pushed Image: ${FULL_IMAGE}'
                """
            }
        }

        stage('Optional Deploy Stage') {
            when {
                expression { return params.DEPLOY_TO_K8S }
            }
            steps {
                sh """
                    set -eu

                    export KUBECONFIG="\$WORKSPACE/.kube/config"
                    mkdir -p "\$WORKSPACE/.kube"

                    kubectl config set-cluster in-cluster \\
                      --server="https://\${KUBERNETES_SERVICE_HOST}:\${KUBERNETES_SERVICE_PORT_HTTPS}" \\
                      --certificate-authority=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt \\
                      --embed-certs=true >/dev/null

                    kubectl config set-credentials jenkins \\
                      --token="\$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" >/dev/null

                    kubectl config set-context jenkins@in-cluster \\
                      --cluster=in-cluster \\
                      --user=jenkins \\
                      --namespace=${params.DEPLOY_NAMESPACE} >/dev/null

                    kubectl config use-context jenkins@in-cluster >/dev/null

                    echo '=== Update Deployment Image ==='
                    kubectl -n ${params.DEPLOY_NAMESPACE} set image deployment/${params.DEPLOYMENT_NAME} ${params.CONTAINER_NAME}=${FULL_IMAGE}

                    echo '=== Rollout Status ==='
                    kubectl -n ${params.DEPLOY_NAMESPACE} rollout status deployment/${params.DEPLOYMENT_NAME} --timeout=180s
                """
            }
        }
    }

    post {
        success {
            echo "warehouse-service image build success: ${env.FULL_IMAGE}"
        }
        failure {
            echo 'warehouse-service image build failed. Check the stage logs above to locate the error.'
        }
    }
}
