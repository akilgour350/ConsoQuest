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
                changeset "server/**"
            }
            steps {
                sshagent(['consoquest-vm-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no root@10.10.10.4 \
                        "ansible-playbook /root/consoquest/server/ansible/deploy.yml \
                        -e repo_url=https://gitlab.jgraham.me/codeyking350/consoquest.git \
                        -e build_number=${BUILD_NUMBER}"
                    '''
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
