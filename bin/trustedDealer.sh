#!/usr/bin/env bash

if [[ $1 =~ ^-?[0-9]+$ ]]
then
    echo "Executing Trusted Dealer"
    ./bin/run.sh --dealer --servers=3 --degree=1 --root=initFiles --keyPath=rsa --time -min=$1
else
    echo "Please supply an integer representing number of minutes for vote"
fi
