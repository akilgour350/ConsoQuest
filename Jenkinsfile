pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Code pulled successfully!'
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    changeset "server/**"
                    changeset "Jenkinsfile"
                }                
            }
            steps {
                sshagent(['consoquest-vm-ssh']) {
                    withCredentials([string(credentialsId: 'consoquest-gitlab-ansible', variable: 'GITLAB_TOKEN')]) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no root@10.10.10.4 \
                            "cd /root/consoquest && git pull"
                            
                            ssh -o StrictHostKeyChecking=no root@10.10.10.4 \
                            "ansible-playbook /root/consoquest/server/ansible/deploy.yml \
                            -e build_number=${BUILD_NUMBER}"
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
