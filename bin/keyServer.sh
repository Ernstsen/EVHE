#!/usr/bin/env bash

echo "Starting key server"

if [[ "$PWD" == bin ]]
then
    ./run.sh --keyServer "$@"
else
    ./bin/run.sh --keyServer "$@"
fi