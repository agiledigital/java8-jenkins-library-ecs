/*
 * Toolform-compatible Jenkins 2 Pipeline build step for building Java 8 apps using Maven on ECS
 */

def call(Map config) {

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

  stage('Install dependency') {
    mvn "install:install-file -Dfile='${config.file}' -DgroupId='${config.groupId}' -DartifactId='${config.artifactId}' -Dversion='${config.version}' -Dpackaging='${config.packaging}' -DgeneratePom='${config.generatePom}'"
  }
}
