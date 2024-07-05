#!/bin/sh

LANG=
JRE_HOME="$CATALINA_HOME/jre"
#
# Unlike standard Tomcat, we ignore value of JAVA_OPTS
JAVA_OPTS="-Djava.security.auth.login.config=$CATALINA_HOME/conf/jaas.config -Djava.awt.headless=true -Dorg.apache.logging.log4j.simplelog.StatusLogger.level=FATAL"
JAVA_OPTS="$JAVA_OPTS  -Xms1024m -Xmx4096m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled"
# uncomment to see on console all log4j message
# JAVA_OPTS="$JAVA_OPTS -Dlog4j.debug=true"
# -XX:MaxPermSize=128m

export LANG
export JRE_HOME
export JAVA_OPTS
