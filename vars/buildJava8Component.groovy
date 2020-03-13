/*
 * Toolform-compatible Jenkins 2 Pipeline build step for building Java 8 apps using Maven on ECS
 */

def call(Map config) {

  final artifactDir = "${config.project}-${config.component}-artifacts"

  final mvn = { cmd ->
    ansiColor('xterm') {
      dir(config.baseDir) {
        configFileProvider([
          configFile(
            fileId: 'maven-config',
            replaceTokens: true,
            variable: 'MAVEN_SETTINGS'
            )
          ]) {
          sh "mvn --batch-mode -s \"$MAVEN_SETTINGS\" ${cmd}"
        }
      }
    }
  }

  stage('Build Details') {
    echo "Project:   ${config.project}"
    echo "Component: ${config.component}"
    echo "BuildNumber: ${config.buildNumber}"
  }

  stage('Clean') {
    mvn "clean"
  }

  stage('Install dependencies') {
    mvn "dependency:resolve"
  }

  stage('Test') {
    mvn "test"
    junit allowEmptyResults: true, testResults: "target/surefire-reports/*.xml"
  }

  stage('Package/install release') {
    mvn "install"
  }

  if(config.stage == 'staging') {

    stage('Prepare archive') {
      sh "mkdir -p ${artifactDir}"
      sh "mkdir -p ${config.baseDir}/target"
      sh "echo '${config.project}/${config.component}/${config.buildNumber}' > ${config.baseDir}/target/jenkins.txt"
      sh "cp -r ${config.baseDir}/target/* ${artifactDir}/"
    }

    stage('Archive to Jenkins') {
      def tarName = "${config.project}-${config.component}-${config.buildNumber}.tar.gz"
      sh "tar -czvf \"${tarName}\" -C \"${artifactDir}\" ."
      archiveArtifacts tarName
    }

  }

}
