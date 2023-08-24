export JAVA_HOME=/P/java-1.8-openjdk


function build() {
  printf "\n----------\n"
  echo "$1"
  cd "$1"
    mvn clean install $2
  cd ..
}


buildAll() {( set -e
  build catalogue-xml-to-xlsx
  build open-xml-reader
  build window-size-save-restore

  build exceptions-manager
  build http-manager
  build version-manager
  build zip-manager
  build http-manager-gui

  build sql-script-executor
  build dcf-webservice-framework
  build progress-bar
  build email-generator
  build efsa-rcl

  build catalogue-browser -Dmaven.test.skip
  build tse-reporting-tool -Dmaven.test.skip

  build external-installer
  build internal-installer
  build interpreting-and-checking-tool -Dmaven.test.skip
)}

buildAll
exit_status=$?
if [ ${exit_status} -ne 0 ]; then
  exit "${exit_status}"
fi
