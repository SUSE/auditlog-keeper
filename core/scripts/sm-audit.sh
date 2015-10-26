#!/bin/sh
#
# LogKeeper launcher
#
# Author: Bo Maryniuk <bo@suse.de>
#

CP=$CLASSPATH
CONFIG="/etc/auditlog-keeper.conf"

LIBS="
commons-logging.jar
tiny-sqlmap.jar
apache-xmlrpc3-client.jar
apache-xmlrpc3-server.jar
apache-xmlrpc3-common.jar
h2.jar
ws-commons-util.jar
auditlog-keeper.jar
postgresql-jdbc.jar
ojdbc14.jar
"

if [ "$EDITOR" = "" ]; then
  EDITOR=/usr/bin/vim
fi

LIB_PATH=/usr/share/java
for f in $LIBS; do
  CP=$CP:$LIB_PATH/$f
done

PLUGINS=/usr/share/auditlog-keeper/plugins
for f in $PLUGINS/*.jar; do
  CP=$CP:$f
done

EXEC="java -cp $CP"
if [ "$1" = "" ]; then
  $EXEC de.suse.logkeeper.Main --daemon "file://$CONFIG"
else
  if [[ "$1" = "--init-database" && "$2" != "" ]]; then
    OUT=`$EXEC com.suse.logkeeper.plugins.RDBMSLog file://$CONFIG $2`
    if [ "$?" != "0" ]; then
      echo "RDBMS plugin is not available at the moment."
      echo
      echo "Error output:"
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

