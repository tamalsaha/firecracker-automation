#!/bin/bash


source variables


ANSIBLE_INVENTORY_DIR=ansible/inventories/eks
ANSIBLE_INVENTORY=$ANSIBLE_INVENTORY_DIR/hosts.yaml
KUBECTL_CONFIG_ANSIBLE_OUTPUT=/tmp/eks_config.yaml

py3_path=$(which python3)
ansplay=$(which ansible-playbook)
ansgala=$(which ansible-galaxy)
py3_man_path=/usr/bin/python3

# Not ideal, should be done by ansible
kcontext=$(kubectl config view --flatten | yq -r '.clusters[] | select(.cluster.server | test("172.26.0") ) | .name')
kubectl config set-context ${kcontext}

$ansgala collection install kubernetes.core

$py3_path $ansplay -i ansible/inventories/eks \
    --extra-vars "kcontext=${kcontext} ansible_python_interpreter=${py3_man_path}"  \
    ansible/openebs.yaml