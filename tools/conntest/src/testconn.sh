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

exec "$JAVA_HOME/bin/java" -jar "$PRGDIR/testconn.jar" $*
