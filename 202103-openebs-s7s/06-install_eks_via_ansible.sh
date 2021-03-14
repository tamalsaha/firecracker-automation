#!/bin/bash


source variables


ANSIBLE_INVENTORY_DIR=ansible/inventories/eks
ANSIBLE_INVENTORY=$ANSIBLE_INVENTORY_DIR/hosts.yaml
KUBECTL_CONFIG_ANSIBLE_OUTPUT=/tmp/eks_config.yaml


# Static part of the inventory
mkdir -p $ANSIBLE_INVENTORY_DIR
cat << EOF > $ANSIBLE_INVENTORY
all:
  vars:
    ansible_ssh_common_args: '-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
    ansible_user: fc
    ansible_ssh_private_key_file: keypairs/kp
EOF


# Dynamically generate Ansible inventory file

## Nodes
cat <<EOF >> $ANSIBLE_INVENTORY
eks_nodes:
  hosts:
EOF

tokens=""
for i in `seq 2 $NUMBER_VMS`
do
	token=`echo $( date +%s )$RANDOM | md5sum | cut -b 1-32`
	cat <<EOF >> $ANSIBLE_INVENTORY
   ${VMS_NETWORK_PREFIX}.$(( $i + 1 )):
     token: $token
EOF
	tokens="$tokens $token"
done


cat <<EOF >> $ANSIBLE_INVENTORY
eks_master:
  hosts:
    ${VMS_NETWORK_PREFIX}.2:
      eks_config_outfile: $KUBECTL_CONFIG_ANSIBLE_OUTPUT
      tokens:
EOF

for token in $tokens
do
	cat <<EOF >> $ANSIBLE_INVENTORY
        - $token
EOF
done


# Call ansible-playbook for eks cluster
ansible-playbook -i ansible/inventories/eks ansible/eks.yaml

# Copy or merge generated kubectl config to ~/.kube/config
if [ -f ~/.kube/config ]
then
	tmpfile=/tmp/.$RANDOM-$RANDOM
	cp ~/.kube/config $tmpfile
	KUBECONFIG=$tmpfile:$KUBECTL_CONFIG_ANSIBLE_OUTPUT kubectl config view --flatten > ~/.kube/config
else
	mkdir -p ~/.kube
	cp $KUBECTL_CONFIG_ANSIBLE_OUTPUT ~/.kube/config
fi
rm $KUBECTL_CONFIG_ANSIBLE_OUTPUT
