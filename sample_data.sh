#!/bin/sh

echo "local.random.diceroll 3.124 `date +%s`" | nc -c 127.0.0.1 2003
