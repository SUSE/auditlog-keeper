#!/bin/sh
#
# LogKeeper launcher
#
# Author: Bo Maryniuk <bo@suse.de>
#

CP=$CLASSPATH
CONFIG="file:///etc/audit-log-keeper.conf"

LIBS="
commons-logging-1.1.jar
tiny-sql-map.jar
xmlrpc-client-3.1.3.jar
xmlrpc-server-3.1.3.jar
xmlrpc-common-3.1.3.jar
h2-1.3.158.jar
ws-commons-util-1.0.2.jar
auditlog-keeper.jar
auditlog-keeper-rdbms.jar
"

if [ "$EDITOR" = "" ]; then
  EDITOR=/usr/bin/vim
fi

LIB_PATH=../lib
for f in $LIBS; do
  CP=$CP:$LIB_PATH/$f
done

PLUGINS=/home/bo/tmp/plugins
for f in $PLUGINS/*.jar; do
  CP=$CP:$f
done

OPTS="-Dcom.sun.management.jmxremote.port=9000 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
EXEC="java $OPTS -cp $CP"
if [ "$1" = "" ]; then
  $EXEC de.suse.logkeeper.Main --daemon $CONFIG
else
  if [[ "$1" = "--init-database" && "$2" != "" ]]; then
    OUT=`$EXEC com.suse.logkeeper.plugins.RDBMSLog $CONFIG $2`
    if [ "$?" != "0" ]; then
      echo "RDBMS plugin is not available at the moment."
      echo
      echo $OUT
    else
      echo $OUT
    fi
  elif [ "$1" = "--configure" ]; then
    sudo $EDITOR $CONFIG
  else
    echo "Usage: $0 [options] [value]

Options:
  --init-database [tag]		Initialize database for a tag in the configuration.
  				Tag is a name in /etc/rhn/log-keeper.conf for a particular
				plugin.

  				Example: $0 --init-database foobar

  --configure			Configure Log Keeper in $EDITOR editor using sudo.
"
  fi
fi

