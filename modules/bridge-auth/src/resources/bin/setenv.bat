
set JRE_HOME=%CATALINA_HOME%\jre
rem
rem Unlike standard Tomcat, we ignore existing settings of JAVA_OPTS.
set JAVA_OPTS= -Djava.security.auth.login.config="%CATALINA_HOME%\conf\jaas.config" -Dderby.system.home="%CATALINA_HOME%\data" -Xms512m -Xmx512m
set JAVA_OPTS=-Djavax.net.ssl.keyStore=conf\BridgeIC.keystore -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStore=conf\BridgeIC.truststore -Djavax.net.ssl.trustStorePassword=changeit %JAVA_OPTS%
set JAVA_OPTS= %JAVA_OPTS% -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=128m

