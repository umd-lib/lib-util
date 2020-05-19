#!/bin/bash

# Test process for use in mpbatch testing

duration=$1
status=$2
stdout_lines=$3
stderr_lines=$4

sleep $duration

i=0
until [ $i -ge $stdout_lines ]
do
  echo line $i
  ((i=i+1))
done

i=0
until [ $i -ge $stderr_lines ]
do
  echo line $i 1>&2
  ((i=i+1))
done

exit $status
