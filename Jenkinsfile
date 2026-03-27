pipeline {
    agent any


    options {
        skipDefaultCheckout(true)
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
        // Must match Jenkins -> Manage Jenkins -> Tools names
        JDK_TOOL = 'JDK-25'
        MAVEN_TOOL = 'Maven-3.9'
        MVN = 'mvn -B -e'
    }


    stages {
        stage('Workspace check') {
            steps {
                sh 'test -f common/pom.xml'
                sh 'test -f config-server/pom.xml'
                sh 'test -f discovery-server/pom.xml'
                sh 'test -f api-gateway/pom.xml'
                sh 'test -f professional-service/pom.xml'
                sh 'test -f booking-service/pom.xml'
            }
        }


        stage('Preflight checks') {
            tools {
                jdk "${env.JDK_TOOL}"
                maven "${env.MAVEN_TOOL}"
            }
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'docker version'
                sh 'git --version'
            }
        }


        stage('Install common') {
            tools {
                jdk "${env.JDK_TOOL}"
                maven "${env.MAVEN_TOOL}"
            }
            steps {
                sh "${MVN} -f common/pom.xml clean install -DskipTests"
            }
        }


        stage('Build and test modules') {
            parallel {
                stage('config-server') {
                    steps { sh "${MVN} -f config-server/pom.xml clean verify" }
                }
                stage('discovery-server') {
                    steps { sh "${MVN} -f discovery-server/pom.xml clean verify" }
                }
                stage('api-gateway') {
                    steps { sh "${MVN} -f api-gateway/pom.xml clean verify" }
                }
                stage('professional-service') {
                    steps { sh "${MVN} -f professional-service/pom.xml clean verify" }
                }
                stage('booking-service') {
                    steps { sh "${MVN} -f booking-service/pom.xml clean verify" }
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
                    passwordVariable: '[Credentials]'
                )]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'


                    sh "${MVN} -f config-server/pom.xml clean package -DskipTests"
                    sh "${MVN} -f discovery-server/pom.xml clean package -DskipTests"
                    sh "${MVN} -f api-gateway/pom.xml clean package -DskipTests"
                    sh "${MVN} -f professional-service/pom.xml clean package -DskipTests"
                    sh "${MVN} -f booking-service/pom.xml clean package -DskipTests"


                    sh "docker build -f config-server/Dockerfile -t ${DOCKER_NAMESPACE}/cleaning-booking-microservices-config-server:latest config-server"
                    sh "docker build -f discovery-server/Dockerfile -t ${DOCKER_NAMESPACE}/cleaning-booking-microservices-discovery-server:latest discovery-server"
                    sh "docker build -f api-gateway/Dockerfile -t ${DOCKER_NAMESPACE}/cleaning-booking-microservices-api-gateway:latest api-gateway"
                    sh "docker build -f professional-service/Dockerfile -t ${DOCKER_NAMESPACE}/cleaning-booking-microservices-professional-service:latest ."
                    sh "docker build -f booking-service/Dockerfile -t ${DOCKER_NAMESPACE}/cleaning-booking-microservices-booking-service:latest ."


                    sh "docker push ${DOCKER_NAMESPACE}/cleaning-booking-microservices-config-server:latest"
                    sh "docker push ${DOCKER_NAMESPACE}/cleaning-booking-microservices-discovery-server:latest"
                    sh "docker push ${DOCKER_NAMESPACE}/cleaning-booking-microservices-api-gateway:latest"
                    sh "docker push ${DOCKER_NAMESPACE}/cleaning-booking-microservices-professional-service:latest"
                    sh "docker push ${DOCKER_NAMESPACE}/cleaning-booking-microservices-booking-service:latest"
                }
            }
        }
    }
}
