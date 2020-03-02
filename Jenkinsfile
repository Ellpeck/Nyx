pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'chmod +x ./gradlew.sh'
        sh './gradlew clean --no-daemon'
        sh './gradlew build --no-daemon'
      }
    }

    stage('Artifacts') {
      steps {
        archiveArtifacts 'build/libs/**.jar'
      }
    }

  }
}