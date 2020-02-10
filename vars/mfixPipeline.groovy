def call(def job) {

    node('master') {
       
      
            try {
                // Clone exa repository, checkout develop branch
                stage('Clone') {
                    sh(libraryResource('mfix/clone_exa.sh'))
                    return
                }
                // Build singularity image
                stage('Build'){
                    sh(libraryResource('mfix/build_exa.sh'))
                }
                // Push to sregistry
                stage('Push') {
                    sh(libraryResource('mfix/push_exa.sh'))
                }
                // Setup sregistry-cli on Summit if needed, submit Slurm job
                stage('Summit') {
                    sshagent(credentials: ['holtat-scompile']) {
                        sh 'ssh -o StrictHostKeyChecking=no -l holtat scompile.rc.int.colorado.edu "echo $SREG_ESCAPED > /home/holtat/sreg_tmp"'
                        sh(libraryResource('mfix/sreg_summit.sh'))
                        sh(libraryResource("${job}"))
                    }
                }
            } finally {
                step([$class: 'WsCleanup'])
            }

       
    }
}
