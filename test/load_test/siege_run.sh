#!/bin/sh

for f in *.txt
do 
    echo $f
    siege -f $f -i -b -c 100 -t 1m 2>&1 | tee $f.siege.out
done