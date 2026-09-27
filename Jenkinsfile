pipeline {
    agent any

    environment {
        HOST_IP = '10.10.10.4'
    }

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
                    sh '''
                        ssh -o StrictHostKeyChecking=no root@${HOST_IP} \
                        "cd /root/consoquest && git pull"
                        
                        ssh -o StrictHostKeyChecking=no root@${HOST_IP} \
                        "ansible-playbook /root/consoquest/server/ansible/deploy.yml \
                        -e build_number=${BUILD_NUMBER}"
                    '''
                }
            }
        }
    }

    post {
        success {
            sshagent(['consoquest-vm-ssh']) {
                sh '''
                    ssh -o StrictHostKeyChecking=no root@${HOST_IP} \
                    "cd /root/consoquest && git push github main"
                '''
            }
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
