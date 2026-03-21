pipeline {
    agent any

    parameters {
        string(name: 'SERVICE_NAME', defaultValue: 'warehouse-service', description: 'Service module name in the backend monorepo.')
        string(name: 'NAMESPACE', defaultValue: 'app', description: 'Kubernetes namespace for deployment.')
        string(
            name: 'IMAGE_REPO',
            defaultValue: 'wuhaotian-acr-registry-vpc.cn-wulanchabu.cr.aliyuncs.com/bestwo/warehouse',
            description: 'Target ACR image repository without tag.'
        )
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Optional image tag. Leave empty to use short Git commit SHA, then fall back to BUILD_NUMBER.'
        )
        string(
            name: 'REGISTRY_CREDENTIALS_ID',
            defaultValue: 'acr-registry',
            description: 'Jenkins username/password credentials id for ACR.'
        )
        string(
            name: 'DEPLOYMENT_NAME',
            defaultValue: 'warehouse-service',
            description: 'Kubernetes Deployment name.'
        )
        string(
            name: 'CONTAINER_NAME',
            defaultValue: 'warehouse-service',
            description: 'Container name inside the Deployment.'
        )
    }

    options {
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Jar') {
            steps {
                script {
                    env.CURRENT_BRANCH = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
                    env.GIT_SHORT_SHA = env.GIT_COMMIT ? env.GIT_COMMIT.take(8) : sh(
                        script: 'git rev-parse --short=8 HEAD',
                        returnStdout: true
                    ).trim()
                    env.RESOLVED_IMAGE_TAG = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : (env.GIT_SHORT_SHA ?: env.BUILD_NUMBER)
                    env.FULL_IMAGE = "${params.IMAGE_REPO}:${env.RESOLVED_IMAGE_TAG}"
                    env.REGISTRY_HOST = params.IMAGE_REPO.tokenize('/')[0]
                }

                sh """
                    echo '=== SCM Context ==='
                    echo 'Branch: ${CURRENT_BRANCH}'
                    echo 'Commit: ${GIT_SHORT_SHA}'
                    echo 'Image: ${FULL_IMAGE}'

                    echo '=== Maven Build ==='
                    cd backend
                    mvn -pl ${params.SERVICE_NAME} -am clean package -DskipTests
                """
            }
        }

        stage('Prepare app.jar') {
            steps {
                sh """
                    set -eu

                    FAT_JAR=\$(find backend/${params.SERVICE_NAME}/target -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' | head -n 1)
                    if [ -z "\$FAT_JAR" ]; then
                      echo 'ERROR: fat jar not found after Maven build'
                      exit 1
                    fi

                    cp "\$FAT_JAR" backend/${params.SERVICE_NAME}/app.jar
                    ls -lh backend/${params.SERVICE_NAME}/app.jar
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    echo '=== Docker Build ==='
                    docker build -f backend/${params.SERVICE_NAME}/Dockerfile -t ${FULL_IMAGE} .
                """
            }
        }

        stage('Push Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: params.REGISTRY_CREDENTIALS_ID,
                        usernameVariable: 'ACR_USERNAME',
                        passwordVariable: 'ACR_PASSWORD'
                    )
                ]) {
                    sh """
                        set -eu

                        echo '=== Docker Login ==='
                        echo "\$ACR_PASSWORD" | docker login ${REGISTRY_HOST} --username "\$ACR_USERNAME" --password-stdin

                        echo '=== Docker Push ==='
                        docker push ${FULL_IMAGE}

                        echo '=== Docker Logout ==='
                        docker logout ${REGISTRY_HOST} || true
                    """
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                sh """
                    set -eu

                    echo '=== Apply Service ==='
                    kubectl -n ${params.NAMESPACE} apply -f deploy/k8s/warehouse-service-service.yaml

                    echo '=== Update Deployment Image ==='
                    kubectl -n ${params.NAMESPACE} set image deployment/${params.DEPLOYMENT_NAME} ${params.CONTAINER_NAME}=${FULL_IMAGE}

                    echo '=== Rollout Status ==='
                    kubectl -n ${params.NAMESPACE} rollout status deployment/${params.DEPLOYMENT_NAME} --timeout=180s

                    echo '=== Running Pods ==='
                    kubectl -n ${params.NAMESPACE} get pods -l app.kubernetes.io/name=${params.SERVICE_NAME} -o wide
                """
            }
        }
    }

    post {
        success {
            echo "warehouse-service deployment succeeded: ${env.FULL_IMAGE}"
        }
        failure {
            echo 'warehouse-service deployment failed. Check the stage logs above to locate the issue.'
        }
    }
}
