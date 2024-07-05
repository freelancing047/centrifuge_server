#!/bin/sh

#
# If you are editing this file, make sure you save in Unix Mode.
# The carriage-return causes incorrect processing by the shell interpreter
#

PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`

CSI_HOME=`cd "$PRGDIR/../.."; pwd`
SCRIPT_DIR=$CSI_HOME/utils/migrate
JRE_HOME=$CSI_HOME/jre

exec "$JRE_HOME/bin/java" -jar "$SCRIPT_DIR/migrate.jar" export "$CSI_HOME" $*
