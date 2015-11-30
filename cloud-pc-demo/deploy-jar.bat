mvn deploy:deploy-file -DgroupId=org.eclipse.paho -DartifactId=mqtt-client -Dversion=0.4.0 -Dpackaging=jar -Dfile=mqtt-client-0.4.0.jar -Durl=http://129.1.77.1:8081/nexus/content/repositories/snapshots/ -DrepositoryId=snapshots

pause
pause