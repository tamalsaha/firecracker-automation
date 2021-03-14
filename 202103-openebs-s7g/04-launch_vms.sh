#!/bin/bash


source variables

function curl_args() {
	curl --unix-socket $socket \
		-H 'Accept: application/json'	\
		-H 'Content-Type: application/json'	\
		$*
}

function firecracker_http_file() {
	curl_args -X $1 'http://localhost/'$2 --data-binary "@"$3
}

function create_tap() {
	local device=$1

	ip addr show $device > /dev/null 2>&1
	if [ $? -ne 0 ]
	then
		sudo ip tuntap add dev $device mode tap
        	sudo ip link set dev $device up
	fi
}

function create_vm_taps() {
	local tap_metadata=$1
	local tap_main=$2

	create_tap $tap_metadata
	create_tap $tap_main

	sudo ip link set $tap_main master $FIRECRACKER_BRIDGE
}

function script_exit() {
	echo -ne "\n\t$1\n\n" >&2
	exit 1
}


function launch_vm() {
	local instance_number=$1
	local tmpfile=/tmp/.$RANDOM-$RANDOM
	local socket=$FIRECRACKER_SOCKET.$instance_number
	local instance_id=`printf "id%05d%05d" $RANDOM $RANDOM`
	local log_file=/tmp/.$instance_id.log
	
	# Start firecracker daemon
	(
		rm -f $socket
		touch $log_file
		$FIRECRACKER --api-sock $socket --log-path $log_file --level Debug &> /dev/null &
		pid=$!
		mkdir -p $FIRECRACKER_PID_DIR
		echo $pid > $FIRECRACKER_PID_DIR/$pid
		echo "Started Firecracker with pid=$pid, logs: $log_file"
	)
	
	# Wait for API server to start
	while [ ! -e $socket ]; do
	    sleep 0.1s
	done
	
	# VM config
	cat conf/firecracker/instance-config.json | \
		./tmpl.sh __INSTANCE_VCPUS__ $VM_VCPUS | \
		./tmpl.sh __INSTANCE_RAM_MB__ $(( $VM_RAM_GB * 1024 )) \
		> $tmpfile
	firecracker_http_file PUT 'machine-config' $tmpfile
	
	# Drives
	mkdir -p disks
	root_fs="./disks/"$( basename $IMAGE_ROOTFS).$instance_id
	cp $IMAGE_ROOTFS $root_fs
	cat conf/firecracker/drives.json | \
		./tmpl.sh __ROOT_FS__ $root_fs \
		> $tmpfile
	firecracker_http_file PUT 'drives/rootfs' $tmpfile
	
	drive_fs="./drives/$instance_id"
	cp images/template.disk $drive_fs
	cat conf/firecracker/data-drives.json | \
		./tmpl.sh __DRIVE_FS__ $drive_fs \
		> $tmpfile
	firecracker_http_file PUT 'drives/drivefs' $tmpfile

	# Networking
	tap_number_base=$(( ($instance_number - 1) * 2 ))
	tap_metadata="tap"`printf "%02d" $tap_number_base`
	tap_main="tap"`printf "%02d" $(( $tap_number_base + 1 ))`
	create_vm_taps $tap_metadata $tap_main
	
	cat conf/firecracker/network_interfaces.eth0.json | \
		./tmpl.sh __TAP_METADATA__ $tap_metadata \
		> $tmpfile
	
	firecracker_http_file PUT 'network-interfaces/eth0' $tmpfile
	
	mac_octet=`printf '%02x' $(( $instance_number + 1 ))`
	cat conf/firecracker/network_interfaces.eth1.json | \
		./tmpl.sh __MAC_OCTET__ $mac_octet | \
		./tmpl.sh __TAP_MAIN__ $tap_main \
		> $tmpfile
	firecracker_http_file PUT 'network-interfaces/eth1' $tmpfile
	
	# Boot configuration
	instance_ip=$VMS_NETWORK_PREFIX"."$(( $instance_number + 1 ))
	network_config_base64=$( \
		cat conf/cloud-init/network_config.yaml | \
		./tmpl.sh __INSTANCE_IP__ $instance_ip | \
		./tmpl.sh __MAC_OCTET__ $mac_octet | \
		./tmpl.sh __GATEWAY__ $VMS_NETWORK_PREFIX".1" | \
		gzip --stdout - | \
		base64 -w 0
	)
	cat conf/firecracker/boot-source.json | \
		./tmpl.sh __KERNEL_IMAGE__ $KERNEL_IMAGE | \
		./tmpl.sh __INSTANCE_ID__ $instance_id | \
		./tmpl.sh __NETWORK_CONFIG__ $network_config_base64 | \
		./tmpl.sh __INITRD__ $INITRD \
		> $tmpfile
	firecracker_http_file PUT 'boot-source' $tmpfile
	
	# Metadata
	cat conf/cloud-init/meta-data.yaml | \
		./tmpl.sh __INSTANCE_ID__ $instance_id | \
		./tmpl.sh __HOSTNAME__ $instance_id | \
		jq --raw-input --slurp '{ "latest": { "meta-data": . }}' \
		> $tmpfile
	firecracker_http_file PUT 'mmds' $tmpfile
	
	# User data
	cat conf/cloud-init/user-data.yaml | \
		./tmpl.sh __SSH_PUB_KEY__ "`cat $KEYPAIR_DIR/$DEFAULT_KP.pub`" | \
		jq --raw-input --slurp '{ "latest": { "user-data": . }}' \
		> $tmpfile
	firecracker_http_file PATCH 'mmds' $tmpfile
	
	# Cleanup
	rm $tmpfile
	
	# Start VM
	firecracker_http_file PUT 'actions' conf/firecracker/instance-start.json
	[ $? -eq 0 ] && echo "Instace $instance_id started. SSH with ssh -i $KEYPAIR_DIR/$DEFAULT_KP fc@$instance_ip"
	
}


# Main
for i in `seq 1 $NUMBER_VMS`
do
	(
		launch_vm $i
	)&
done
wait
