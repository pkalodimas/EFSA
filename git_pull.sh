directories=(\
  catalogue-browser\
  catalogue-xml-to-xlsx\
  dcf-webservice-framework\
  efsa-rcl\
  email-generator\
  exceptions-manager\
  external-installer\
  http-manager\
  http-manager-gui\
  internal-installer\
  interpreting-and-checking-tool\
  open-xml-reader\
  progress-bar\
  sql-script-executor\
  tse-reporting-tool\
  version-manager\
  window-size-save-restore\
  zip-manager\
)

eval `ssh-agent -s`
ssh-add ~/.ssh/*_tr

for dir in "${directories[@]}";
  do (
    printf "\n----------\n"
    echo "$dir"
    cd "$dir" || return
    pwd
    #git clone "https://github.com/aapostolou-tr/${dir}.git"
	git stash
    git pull --rebase
    git checkout v204
    git pull --rebase
    git stash pop
    cd ..
  )
done

