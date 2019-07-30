pipeline {
    agent any
    
    stages {
        stage ('CodeBuild') {
            agent {
                docker {
                    image 'maven:3.5-jdk-8-alpine'
                }
            }
            
            steps {
                sh 'mvn clean package -DskipTests'
                stash includes:'**/target/auth-*.war', name:'warfile'
                stash includes: '**/**', name: 'Source'
            }
        }
        
        stage ('Commit-Time SAST') {
            agent {
                docker {
                    image 'cov-analysis'
                    args '--entrypoint="" --network="host"'
                }
             }
             steps {
                print 'Run Coverity Scan'
                unstash 'Source'
                sh 'mvn --version'
                sh 'wget http://cicd-agent.cigital.com:9091'
                sh '/opt/cov-analysis-linux64-2018.09/bin/cov-build --dir /opt/cov-analysis-linux64-2018.09/idir --return-emit-failures --delete-stale-tus mvn clean -Dmaven.test.skip=true install'
                sh '/opt/cov-analysis-linux64-2018.09/bin/cov-emit-java --dir /opt/cov-analysis-linux64-2018.09/idir --war target/auth-2.1.1.RELEASE.war'
                sh '/opt/cov-analysis-linux64-2018.09/bin/cov-analyze --dir /opt/cov-analysis-linux64-2018.09/idir --webapp-security --strip-path $(pwd)'
                sh '/opt/cov-analysis-linux64-2018.09/bin/cov-commit-defects --encryption none --dir /opt/cov-analysis-linux64-2018.09/idir --host ${COV_HOST} --port ${COV_PORT} --user ${COV_USER} --password ${COV_PASS} --stream BSIMM-Sample' 
                sh '/opt/cov-analysis-linux64-2018.09/bin/cov-commit-defects --encryption none --dir /opt/cov-analysis-linux64-2018.09/idir --host ${COV_HOST} --port ${COV_PORT} --user ${COV_USER} --password ${COV_PASS} --stream BSIMM-Sample --preview-report-v2 $WORKSPACE/preview.json'
                sh 'ls -la /opt/cov-analysis-linux64-2018.09/idir/output'
                stash includes: '**/target/auth-2.1.1.RELEASE.war', name: 'cov-warfile'
                stash includes: '**/preview.json', name: 'coverity-preview'
                
             }
        }
        
        stage ('Build-Time SCA') {
            parallel {
                stage('Black Duck Binary Analysis SCA') {
                    agent {
                        docker {image 'python:custom'}
                    }
                    steps {
                        echo 'Running Black Duck Binary Analysis SCA'
                        //unstash 'Scripts'
                        //sh 'cp ${WORKSPACE}/scripts/protecodeSC_demo.py ${WORKSPACE}'
                        //unstash 'warfile'
                        //sh 'ls $(pwd)'
                        //sh 'python ${WORKSPACE}/protecodeSC_demo.py --app $(pwd)/target/insecure-bank.war --protecode-host protecode-sc.com --protecode-username ${PROTECODE_USER} --protecode-password ${PROTECODE_PASS} --protecode-group Synopsys-SIG-Internal'
                        //archiveArtifacts allowEmptyArchive: true, artifacts: '**/*.csv'
                    }
                }
                stage('Black Duck SCA') {
                    steps {
                        echo 'Running Black Duck SCA'
                        unstash 'Source'
                        unstash 'warfile'
                        sh 'ls $(pwd)'
                        sh 'ls $(WORKSPACE)'
                        /*sh '''#!/bin/bash
                        bash <(curl -s https://blackducksoftware.github.io/hub-detect/hub-detect.sh) --blackduck.hub.url="https://blackduck-integration.cigital.com/" --blackduck.hub.username=${BLACKDUCK_USER} --blackduck.hub.password=${BLACKDUCK_PASSWORD} --blackduck.hub.trust.cert=true --blackduck.hub.auto.import.cert=true > $WORKSPACE/log.txt''' 
        			    sh 'cat $WORKSPACE/log.txt'*/
        			    
        			    sh 'wget https://test-repo.blackducksoftware.com/artifactory/bds-integrations-release/com/blackducksoftware/integration/hub-detect/4.4.2/hub-detect-4.4.2.jar'
        			    //sh 'java -jar $(pwd)/hub-detect-4.4.2.jar --blackduck.hub.url=https://blackduck-integration.cigital.com/ --blackduck.api.token=ZGRkM2YyZGMtZmUyYy00Y2JiLWE4MTgtMWRkN2U1YTViNDA4Ojk0Njk2NDA0LWQ2NzEtNGNlZC1iYzQ2LTQzNGE4NGQyM2M2OQ== --blackduck.hub.trust.cert=true --blackduck.hub.auto.import.cert=true | tee log.txt'
        			    //sh 'java -jar $(pwd)/SCT/Adapters/blackduck-api.jar blackduck-integration.cigital.com ${BLACKDUCK_USER} ${BLACKDUCK_PASSWORD} log.txt $WORKSPACE/BlackDuck-Report.json'
        			    
        			    /* For temp*/
        			    sh 'java -jar $(pwd)/hub-detect-4.4.2.jar --blackduck.hub.url=https://hubtesting.blackducksoftware.com --blackduck.api.token=MTRhYzYxYmItZWJhMS00Njg1LTgwMTMtOGIyMDQzMGE4MDQzOjE2YjhmZjNiLTY2NGYtNDAwZC04ZjUyLThlNDJmN2M0NmU2ZQ== --blackduck.hub.trust.cert=true --blackduck.hub.auto.import.cert=true | tee log.txt'
        			    sh 'java -jar $(pwd)/SCT/Adapters/blackduck-api.jar hubtesting.blackducksoftware.com sysadmin F@w0m0! log.txt $WORKSPACE/BlackDuck-Report.json'
        			    
        				archiveArtifacts '**/BlackDuck-Report.json'
                    }
                }
            }
        }      
        stage ('Test-time Deploy') {
            steps {
                print 'Deploy to Tomcat'
                unstash 'warfile'
                sh 'sudo /opt/deployment/tomcat/apache-tomcat-8.5.28/bin/shutdown.sh'
                sh 'sudo cp $(pwd)/target/insecure-bank.war /opt/deployment/tomcat/apache-tomcat-8.5.28/webapps'
              
                sh 'sudo /opt/deployment/tomcat/apache-tomcat-8.5.28/bin/startup.sh'
                sleep 45
                print 'Application Deployed to test'
            }
        }
        
        stage ('Test-time IAST') {
            steps {
                print 'Execute DAST payload'
                //execute Arachni
                sh '/opt/deployment/arachni/arachni-1.5.1-0.5.12/bin/arachni --checks=csrf http://cicdtraining.cigital.com:8181/insecure-bank --report-save-path=Arachni-report.afr'
                sh '/opt/deployment/arachni/arachni-1.5.1-0.5.12/bin/arachni_reporter Arachni-report.afr --report=xml:outfile=Arachni-Report.xml'
                sleep 30
                sh "curl -X GET --insecure --header 'Accept: application/json' --header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTZWVrZXIiLCJuYW1lIjoiSmVua2luc0FjY2VzcyIsInR5cGUiOiJhcGkiLCJleHAiOjE1MjAyODUzODF9.uj29Tfoio0mC7zRS0nc77HWyAPLdBdYFGARpOTWAMt4' 'https://seeker-ent.cigital.com:8481/rest/api/latest/vulnerabilities?projectKeys=default' > Seeker-Report.json"
                archiveArtifacts 'Seeker-Report.json,**/Arachni-Report.xml'
                stash includes: '**/Seeker-Report.json', name: 'SeekerReport'
            }
        }
     
       	stage("SonarQube Security Gate") { 
            steps{
                timeout(time: 1, unit: 'HOURS') { 
                script{
                        def scannerHome = tool name: 'SonarScanner3', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                        withSonarQubeEnv('DemoSonarQube') {
                            def securityGate = waitForQualityGate() 
                            if (securityGate.status != 'OK') {
                                print 'Email sent'
                                emailext subject: "FAILURE - Open Source Major Pipeline: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'", body: """<p>FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p> <p>The job has failed. </p> <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>""", to: "skokil@cigital.com"
                                print 'Pipeline has finished with error.'
                                error "Pipeline aborted due to quality gate failure: ${securityGate.status}"
                            }
                        }    
                }
				} 
			}
		}

    }
    
    post {
        always {
            deleteDir()
        }
    }
}
