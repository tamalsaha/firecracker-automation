#!/bin/bash
# This only generates an empty disk layout

source variables

function generate_drive() {
	echo "Generating $DRIVE_TEMPLATE..."
    [ -e drives ] || mkdir -p drives/
	truncate -s $DRIVE_TEMPLATE_SIZE $DRIVE_TEMPLATE
	mkfs.ext4 $DRIVE_TEMPLATE > /dev/null 2>&1
}

[ ! -f $DRIVE_TEMPLATE ] && generate_drive
