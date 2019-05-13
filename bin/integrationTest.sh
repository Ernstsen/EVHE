#!/usr/bin/env bash

echo "Starting Integration test"

./bin/run.sh --integrationTest --duration=2 --vote -start -end -after
