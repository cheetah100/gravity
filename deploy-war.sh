mvn clean install -DskipTests
cp -vf target/*.war ~/tomcat/webapps
rm -rf ~/tomcat/webapps/gravity

