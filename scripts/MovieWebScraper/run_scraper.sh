#!/bin/bash

if [ "$1" = "--help" ]; then
    echo "Usage ./run_from_to.sh [<sleep>] [<timeout>]"
    echo "e.g. (default values) ./run_from_to.sh 5 30s "
    echo "Sleep between requests is <sleep> seconds,"
    echo "script is killed if not responding after <timeout>."
    exit 1
fi

sleep=${1:-5}
timeout=${2:-30s}

i=0
while true; do
    sleep $sleep
    echo "--- EXECUTING $i ---"
    timeout $timeout python3 MongoDbManager.py
    i=$[$i+1]
done