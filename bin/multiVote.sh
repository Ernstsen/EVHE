#!/usr/bin/env bash

if [[ $1 =~ ^-?[0-9]+$ ]]
then
    echo "Casting random votes to Bulletin Board @localhost:8080"
    ./bin/run.sh --client --multi=$1  --port=8080
else
    echo "Please supply an integer representing number of votes"
fi
