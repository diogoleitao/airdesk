#!/bin/bash

if [ -z "$TERMITE_CLI_PATH" ]
then 
	echo "Error: environment variable TERMITE_CLI_PATH undefined."
	exit -1
fi

jline=$TERMITE_CLI_PATH/libs/jline-2.13.jar
groovy=$TERMITE_CLI_PATH/libs/groovy-all-2.3.6.jar
termite=$TERMITE_CLI_PATH/libs/Termite-Cli.jar
deps="$jline:$groovy:$termite"

java -cp $deps pt.inesc.termite.cli.Main $@
