# Firecracker automation for cloud images + initialization via cloud-init

**Source code for the blog post
"[Automation to run VMs based on vanilla Cloud Images on Firecracker](https://ongres.com/blog/automation-to-run-vms-based-on-vanilla-cloud-images-on-firecracker)"**

This directory contains some scripts to help automate the process of creating
virtual machines with [Firecracker](https://firecracker-microvm.github.io/).
Regular cloud images can be used, the scripts present prepare them for usage
with Firecracker. Finally, basic instance initialization is performed via
cloud-init.

Edit the `variables` file to suit your needs.

Follow the scripts in the specified order. Some basic Linux CLI tools are
required like `curl`, `jq`, `tar` with `xz` support, `ssh-keygen`, `iptables`,
the `binutils` package and possibly others.

A Linux bridge will be created in the host and will be NATTed to the main
Internet egress device on the host to give connectivity to the created VMs,
which should also be able to connect among them. Default network range for the
created VMs is `172.26.0.0/24`.

This is not a proper software project, it's just for demonstrating purposes.
Modify scripts at your convenience. It has only been tested with `bionic`
[Ubuntu cloud images](https://cloud-images.ubuntu.com/), but in principle should
work with minor modifications with any Linux cloud image that supports
initialization via `cloud-init`.


## Requirements

Install Ansible's kubernetes.core:

```
ansible-galaxy collection install kubernetes.core
```

Install Helm

```
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
```


# Optional: create EKS cluster on Firecracker VMs

Once the VMs have been created, you may want to run script
`05-install_eks_via_ansible.sh`. This requires you to have Ansible (2.9+)
installed (see Ansible source code in `ansible/` folder). It will create an
[EKS-D compatible](https://snapcraft.io/eks) cluster and will generate/add a
valid `~/.kube/config` configuration in your (local) environment (provided you
have `kubectl` installed). After running this script you should be able to
`kubectl cluster-info` and `kubectl get nodes` successfully.

Please review the `variables` file to ensure you will be creating the desired
number of nodes. At least `4GB` of RAM are recommended per VM.

You can obviously chain commands. While the VM creation script does not know
exactly when the `user-data` part is finished (as it happens within the VM), it
is quite fast, and usually (YMMV) waiting a bit works well. So once scripts `01`
to `03` are run, you may do something like:

```sh
time (./04-launch_vms.sh ; sleep 1m; ./05-install_eks_via_ansible.sh )
```

to create the VMs and EKS cluster in a single line.
