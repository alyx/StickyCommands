// Scripted Pipeline
// Requires libraries from https://github.com/Prouser123/jenkins-tools
// Made by @Prouser123 for https://ci.jcx.ovh.

node('docker-cli') {
  cleanWs()

  docker.image('jcxldn/jenkins-containers:jdk11-mvn-ubuntu').inside {

    stage('Setup') {
      def scmVars = checkout scm
      env.GIT_BRANCH = scmVars.GIT_BRANCH
      env.GIT_URL = scmVars.GIT_URL
      env.GIT_COMMIT = scmVars.GIT_COMMIT
      
      withCredentials([usernamePassword(credentialsId: '83a5a4e6-f6ff-4b2c-9d87-b9fb30c1d9d1', passwordVariable: 'GHPKG_PASS', usernameVariable: 'GHPKG_USER')]) {
	    sh """
	      set +x
	      mkdir -p ~/.m2
	      echo "<settings xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'><servers><server><id>github</id><username>$GHPKG_USER</username><password>$GHPKG_PASS</password></server></servers></settings>" > ~/.m2/settings.xml
	    """
      }
    }

    stage('Build') {
      sh 'mvn clean install'
        
      archiveArtifacts artifacts: 'target/StickyCommands-*.jar', fingerprint: true
				
      ghSetStatus("The build passed.", "success", "ci")
    }
  }
}
