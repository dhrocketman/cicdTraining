pipeline {
    agent any
   
    stages {
        stage ('CodeBuild') {
            steps {
                git 'https://sneha-kokil@bitbucket.org/sig-devsecops/spring-boot-sample.git'
                sh 'mvn clean package -DskipTests'
                stash includes:'**/target/auth-*.war', name:'warfile'
                stash includes: '**/**', name: 'Source'
            }
        }
       
       
        stage ('SAST') {
            steps {
                unstash 'Source'
                withCoverityEnvironment(coverityInstanceUrl: 'http://localhost:8088/', projectName: 'BSIMM-Sample', streamName: 'BSIMM-Sample', viewName: '') {
                    withCredentials([usernamePassword(credentialsId: 'Coverity-Connect-Creds', passwordVariable: 'COV_PASS', usernameVariable: 'COV_USER')]) {
                        sh '$COVERITY_TOOL_HOME/bin/cov-build --dir /opt/coverity-workdir/idir --return-emit-failures --delete-stale-tus mvn clean -Dmaven.test.skip=true install'
                        sh '$COVERITY_TOOL_HOME/bin/cov-emit-java --dir /opt/coverity-workdir/idir --war target/auth-2.1.1.RELEASE.war'
                        sh '$COVERITY_TOOL_HOME/bin/cov-analyze --dir /opt/coverity-workdir/idir --webapp-security --strip-path $(pwd)'
                        sh '$COVERITY_TOOL_HOME/bin/cov-commit-defects --encryption none --dir /opt/coverity-workdir/idir --url $COV_URL --user ${COV_USER} --password ${COV_PASS} --stream $COV_STREAM'
                    }
                       
                }
            }
        }
       
        stage('SCA') {
            steps {
                unstash 'Source'
                unstash 'warfile'
               
                sh 'ls -la'
                synopsys_detect detectProperties: '--detect.source.path=.', returnStatus: true
             
            }
        }
       
        stage('SonarQube Security Gate') {
            steps {
                
                script {
                    withSonarQubeEnv('SonarQube-Local') {
                        def scannerhome = tool name: 'SonarScanner-Local', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                        sh "${scannerhome}/bin/sonar-scanner -Dsonar.java.binaries=target"
                    }
                   
                    sleep 30
                   /*
                    timeout(time: 1, unit: 'HOURS') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                          //slackSend channel: '#bsimm', color: 'danger', message: 'The security gate has failed. Please schedule deep-dive manual code review.'
                          //input message: 'Security Gate Failed. Do you want to proceed?', submitter: 'admin'
                          error 'The security gate failed. Breaking the CI build.'
                        }
                    } */
                }
            }
        }
        stage('IAST') {
            steps{
                script {
                    
                   print 'Deploy to Tomcat'
                   unstash 'warfile'
                   sh 'mv ./target/*.war app.war'
                   sh 'sudo cp app.war /opt/tomcat/webapps/'
                   
                   sh 'sudo systemctl start tomcat.service'
                   sh 'sudo systemctl start zap.service'
                   
                   sleep 60
                   sh 'wget http://localhost:8181/app'
                   
                   sh 'zap-cli -p 2375 status -t 120'
                   sh 'zap-cli -p 2375 open-url http://localhost:8181/app'
                   sh 'zap-cli -p 2375 spider http://localhost:8181/app'
                   sh 'zap-cli -p 2375 active-scan -r http://localhost:8181/app'
                }
            }
        } 
    }
    post {
        always {
            sh 'sudo systemctl stop tomcat.service'
            sh 'sudo systemctl stop zap.service'
        }
    }
}