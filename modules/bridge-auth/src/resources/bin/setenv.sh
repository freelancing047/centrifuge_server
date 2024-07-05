#!/bin/sh

JRE_HOME="$CATALINA_HOME/jre"
#
# Unlike standard Tomcat, we ignore value of JAVA_OPTS
JAVA_OPTS="-Djavax.net.ssl.keyStore=conf/BridgeIC.keystore -Djavax.net.ssl.keyStorePassword=changeit"
JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=conf/BridgeIC.truststore -Djavax.net.ssl.trustStorePassword=changeit"
JAVA_OPTS="$JAVA_OPTS -Djava.security.auth.login.config=$CATALINA_HOME/conf/jaas.config -Dderby.system.home=$CATALINA_HOME/data -Xms512m -Xmx512m"
JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=128m"
export JRE_HOME
export JAVA_OPTS
