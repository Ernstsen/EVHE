#!/usr/bin/env bash

echo "Starting public server"
if [[ "$PWD" == bin ]]
then
    ./run.sh --publicServer "$@"
else
    ./bin/run.sh --publicServer "$@"
fi