pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 120, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        booleanParam(
            name: 'PUSH_DOCKER_IMAGES',
            defaultValue: false,
            description: 'Build and push Docker images (requires docker-hub credentials)'
        )
        string(
            name: 'DOCKER_NAMESPACE',
            defaultValue: '',
            description: 'Docker Hub namespace (for example: mydockeruser)'
        )
    }

    environment {
        // Set these names to exactly match Jenkins -> Manage Jenkins -> Tools
        JDK_TOOL = 'JDK-25'
        MAVEN_TOOL = 'Maven-3.9'
        MVN = 'mvn -B -e'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Preflight checks') {
            tools {
                jdk "${env.JDK_TOOL}"
                maven "${env.MAVEN_TOOL}"
            }
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker version'
                bat 'git --version'
            }
        }

        stage('Install common') {
            tools {
                jdk "${env.JDK_TOOL}"
                maven "${env.MAVEN_TOOL}"
            }
            steps {
                bat "${MVN} -f common/pom.xml clean install -DskipTests"
            }
        }

        stage('Build and test modules') {
            parallel {
                stage('config-server') {
                    steps {
                        bat "${MVN} -f config-server/pom.xml clean verify"
                    }
                }
                stage('discovery-server') {
                    steps {
                        bat "${MVN} -f discovery-server/pom.xml clean verify"
                    }
                }
                stage('api-gateway') {
                    steps {
                        bat "${MVN} -f api-gateway/pom.xml clean verify"
                    }
                }
                stage('professional-service') {
                    steps {
                        bat "${MVN} -f professional-service/pom.xml clean verify"
                    }
                }
                stage('booking-service') {
                    steps {
                        bat "${MVN} -f booking-service/pom.xml clean verify"
                    }
                }
            }
        }

        stage('Publish test results') {
            steps {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
            }
        }

        stage('Docker build and push') {
            when {
                allOf {
                    expression { params.PUSH_DOCKER_IMAGES }
                    expression { params.DOCKER_NAMESPACE?.trim() }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat 'echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin'

                    bat "${MVN} -f config-server/pom.xml clean package -DskipTests"
                    bat "${MVN} -f discovery-server/pom.xml clean package -DskipTests"
                    bat "${MVN} -f api-gateway/pom.xml clean package -DskipTests"
                    bat "${MVN} -f professional-service/pom.xml clean package -DskipTests"
                    bat "${MVN} -f booking-service/pom.xml clean package -DskipTests"

                    bat "docker build -f config-server/Dockerfile -t %DOCKER_NAMESPACE%/cleaning-booking-microservices-config-server:latest config-server"
                    bat "docker build -f discovery-server/Dockerfile -t %DOCKER_NAMESPACE%/cleaning-booking-microservices-discovery-server:latest discovery-server"
                    bat "docker build -f api-gateway/Dockerfile -t %DOCKER_NAMESPACE%/cleaning-booking-microservices-api-gateway:latest api-gateway"
                    bat "docker build -f professional-service/Dockerfile -t %DOCKER_NAMESPACE%/cleaning-booking-microservices-professional-service:latest ."
                    bat "docker build -f booking-service/Dockerfile -t %DOCKER_NAMESPACE%/cleaning-booking-microservices-booking-service:latest ."

                    bat "docker push %DOCKER_NAMESPACE%/cleaning-booking-microservices-config-server:latest"
                    bat "docker push %DOCKER_NAMESPACE%/cleaning-booking-microservices-discovery-server:latest"
                    bat "docker push %DOCKER_NAMESPACE%/cleaning-booking-microservices-api-gateway:latest"
                    bat "docker push %DOCKER_NAMESPACE%/cleaning-booking-microservices-professional-service:latest"
                    bat "docker push %DOCKER_NAMESPACE%/cleaning-booking-microservices-booking-service:latest"
                }
            }
        }
    }
}
