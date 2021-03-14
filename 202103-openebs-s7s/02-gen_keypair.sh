#!/bin/bash

source variables

mkdir -p $KEYPAIR_DIR
ssh-keygen -t ed25519 -q -N "" -f $KEYPAIR_DIR/$DEFAULT_KP
