build/centrifuge-server/bin/catalina.sh stop
sleep 2
ant build
sleep 2
build/centrifuge-server/bin/catalina.sh start
tail -f build/centrifuge-server/logs/catalina.out 
