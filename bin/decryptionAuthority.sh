#!/usr/bin/env bash

echo "Starting Decryption Authority"
if [[ $1 =~ ^-?[0-9]+$ ]]
then
    ./bin/run.sh --authority --conf=initFiles/"$1" --port=808"$1"
else
    echo "Please supply an integer id for this instance"
fi
