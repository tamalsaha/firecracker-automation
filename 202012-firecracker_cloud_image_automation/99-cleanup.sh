#!/bin/bash


source variables


instance_count=0
for i in `seq 0 $(( $NUMBER_VMS - 1 ))`
do
	tap_metadata_id=`printf "%02d" $(( $i * 2 ))`
	tap_main_id=`printf "%02d" $(( $i * 2 + 1 ))`
	
	sudo ip link delete tap${tap_metadata_id}
	sudo ip link delete tap${tap_main_id}
done

sudo ip link delete $FIRECRACKER_BRIDGE

rm -rf disks
rm -rf images
rm -rf keypairs
[ -f ansible/inventories/eks/hosts.yaml ] && rm ansible/inventories/eks/hosts.yaml
