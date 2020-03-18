#!/bin/bash

if [ $# -le 1 ]; then
    echo "Usage ./run_from_to.sh <from> <to>"
    echo "e.g. ./run_from_to.sh 0 12500"
    echo "Sleep between requests is 5 seconds"
    exit 1
fi

from=$1
to=$2

echo $from > UpdateIndex.txt

while [ $(cat UpdateIndex.txt) -lt $to ]; do
    sleep 5
    echo "--- EXECUTING # $(cat UpdateIndex.txt)"
    python3 MongoDbManager.py
done