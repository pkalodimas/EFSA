cd automation/java/libs

call mvn install:install-file -Dfile=swt_win64_3.7.1.v3738a.jar -DgroupId=org.eclipse.platform -DartifactId=swt.win64 -Dversion=3.655 -Dpackaging=jar
call mvn install:install-file -Dfile=org.eclipse.jface_3.7.0.I20110522-1430.jar -DgroupId=org.eclipse.platform -DartifactId=jface -Dversion=3.7.0 -Dpackaging=jar
