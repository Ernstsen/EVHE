#!/usr/bin/env bash

if [[ $1 =~ ^-?[0-1]$ ]]
then
    echo "Dispatching vote"
    ./bin/run.sh --client --read=false --vote=$1
else
    echo "Please supply an integer in {0,1} representing vote"
fi
