#!/bin/bash


source variables


for i in `find $FIRECRACKER_PID_DIR -type f`
do
	kill `cat $i`
	rm $i
done
