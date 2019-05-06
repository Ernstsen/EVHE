#!/usr/bin/env bash

echo "Starting public server"
if [[ $1 =~ ^-?[0-1]+$ ]]
then
    ./bin/run.sh --client --read=false  --port=8080 --vote=$1
else
    echo "Please supply an integer in {0,1} representing vote"
fi
