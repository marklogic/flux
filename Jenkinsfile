def runtests(){
  sh label:'mlsetup', script: '''#!/bin/bash
          cd $WORKSPACE/flux;
          sudo /usr/local/sbin/mladmin stop;
          sudo /usr/local/sbin/mladmin remove;
          mkdir -p $WORKSPACE/flux/docker/sonarqube;
          docker-compose up -d --build;
          sleep 30s;
          curl "http://localhost:8008/api/pull" -d '{"model":"all-minilm"}'
        '''
  script{
    timeout(time: 60, unit: 'SECONDS') {
      waitUntil(initialRecurrencePeriod: 20000) {
        try{
          sh 'curl  --anyauth --user admin:admin -X GET http://localhost:8000/v1/ping'
            return true
        }catch(exception){
             return false
        }
      }
    }
  }
  sh label:'runtests', script: '''#!/bin/bash
    export JAVA_HOME=`eval echo "$JAVA_HOME_DIR"`;
    export GRADLE_USER_HOME=$WORKSPACE$GRADLE_DIR;
    export PATH=$JAVA_HOME/bin:$GRADLE_USER_HOME:$PATH;
    cd $WORKSPACE/flux;
    ./gradlew -i  mlDeploy;
    wget https://www.postgresqltutorial.com/wp-content/uploads/2019/05/dvdrental.zip;
    unzip dvdrental.zip -d docker/postgres/ ;
    docker exec -i docker-tests-flux-postgres-1 psql -U postgres -c "CREATE DATABASE dvdrental";
    docker exec -i docker-tests-flux-postgres-1 pg_restore -U postgres -d dvdrental /opt/dvdrental.tar;
    cd $WORKSPACE/flux/;
    ./gradlew --refresh-dependencies clean test || true;
  '''
  junit '**/*.xml'
}

def postCleanup(){
  sh label:'mlcleanup', script: '''#!/bin/bash
    cd $WORKSPACE/flux;
    sudo /usr/local/sbin/mladmin delete $WORKSPACE/flux/docker/marklogic/logs/;
    docker exec -i --privileged --user root flux-caddy-load-balancer-1 /bin/sh -c "chmod -R 777 /data" || true;
    docker exec -i --privileged --user root flux-sonarqube-1 /bin/sh -c "chmod -R 777 /opt/sonarqube" || true;
    docker-compose rm -fsv || true;
    echo "y" | docker volume prune --filter all=1 || true;
  '''
}

def runSonarScan(String javaVersion){
    sh label:'test', script: '''#!/bin/bash
      export JAVA_HOME=$'''+javaVersion+'''
      export GRADLE_USER_HOME=$WORKSPACE/$GRADLE_DIR
      export PATH=$GRADLE_USER_HOME:$JAVA_HOME/bin:$PATH
      cd flux
     ./gradlew sonar -Dsonar.projectKey='ML-DevExp-marklogic-flux' -Dsonar.projectName='ML-DevExp-marklogic-flux' || true
    '''
}

pipeline{
  agent none

  options {
    checkoutToSubdirectory 'flux'
    buildDiscarder logRotator(artifactDaysToKeepStr: '7', artifactNumToKeepStr: '', daysToKeepStr: '30', numToKeepStr: '')
  }

  environment{
    JAVA_HOME_DIR="/home/builder/java/jdk-11.0.2"
    JAVA17_HOME_DIR="/home/builder/java/jdk-17.0.2"
    GRADLE_DIR   =".gradle"
    DMC_USER     = credentials('MLBUILD_USER')
    DMC_PASSWORD = credentials('MLBUILD_PASSWORD')
  }

  stages{

    stage('tests'){
      environment{
        scannerHome = tool 'SONAR_Progress'
      }
      agent{ label 'devExpLinuxPool'}
      steps{
        runtests()
        withSonarQubeEnv('SONAR_Progress') {
          runSonarScan('JAVA17_HOME_DIR')
        }
      }
      post{
        always{
          postCleanup()
        }
      }
    }

    stage('publishApi'){
      agent {label 'devExpLinuxPool'}
      when {
        branch 'develop'
      }
      steps{
        sh label:'publishApi', script: '''#!/bin/bash
          export JAVA_HOME=`eval echo "$JAVA_HOME_DIR"`;
          export GRADLE_USER_HOME=$WORKSPACE/$GRADLE_DIR
          export PATH=$JAVA_HOME/bin:$GRADLE_USER_HOME:$PATH;
          ./gradlew clean;
          cp ~/.gradle/gradle.properties $GRADLE_USER_HOME/gradle.properties;
          cd $WORKSPACE/flux;
          ./gradlew publish
        '''
      }
    }

    stage('publish'){
      agent{ label 'devExpLinuxPool'}
      when {
        branch 'develop'
      }
      steps{
        script{
          sh label:'publish', script: '''#!/bin/bash
            export JAVA_HOME=`eval echo "$JAVA_HOME_DIR"`;
            export GRADLE_USER_HOME=$WORKSPACE$GRADLE_DIR;
            export PATH=$JAVA_HOME/bin:$GRADLE_USER_HOME:$PATH;
            cd $WORKSPACE/flux;
            ./gradlew clean;
            ./gradlew distZip;
          '''
          archiveArtifacts artifacts: '**/build/**/*.zip', followSymlinks: false
          def artifactory = Artifactory.newServer(url: 'https://bed-artifactory.bedford.progress.com:443/artifactory/', credentialsId: 'builder-credentials-artifactory')
          def uploadSpec = """{
            "files": [
              {
                "pattern": "${WORKSPACE}/**/build/**/*.zip",
                "target": "ml-generic-dev-tierpoint/flux/",
                "props": "build.number=${BUILD_NUMBER};build.name=${JOB_NAME}"
              }
             ]
            }"""
            artifactory.upload(uploadSpec)
            echo "${uploadSpec}"
        }
      }
    }

    stage('regressions'){
      when{
        allOf{
          branch 'develop'
        }
      }
      environment{
        JAVA_HOME_DIR="/home/builder/java/jdk-17.0.2"
        GRADLE_DIR   =".gradle"
      }
      agent{ label 'devExpLinuxPool'}
      steps{
        runtests()
      }
      post{
        always{
          postCleanup()
        }
      }
    }

  }
}
