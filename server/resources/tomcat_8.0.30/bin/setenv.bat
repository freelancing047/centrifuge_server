
set JRE_HOME=%CATALINA_HOME%\jre
rem
rem Unlike standard Tomcat, we ignore existing settings of JAVA_OPTS.
set JAVA_OPTS= -Djava.security.auth.login.config="%CATALINA_HOME%\conf\jaas.config" -Djava.awt.headless=true -Dorg.apache.logging.log4j.simplelog.StatusLogger.level=FATAL
set JAVA_OPTS= %JAVA_OPTS% -Xms1024m -Xmx1024m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled
rem uncomment to see on console all log4j message
rem set JAVA_OPTS= %JAVA_OPTS% -Dlog4j.debug=true 
rem -XX:MaxPermSize=128m