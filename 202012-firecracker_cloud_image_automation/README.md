# Firecracker automation for cloud images + initialization via cloud-init

This directory contains some scripts to help automate the process of creating
virtual machines with [Firecracker](https://firecracker-microvm.github.io/).
Regular cloud images can be used, the scripts present prepare them for usage
with Firecracker. Finally, basic instance initialization is performed via
cloud-init.

Edit the `variables` file to suit your needs.

Follow the scripts in the specified order. Some basic Linux CLI tools are
required like `curl`, `jq`, `tar` with `xz` support, `ssh-keygen`, `iptables`
and possibly others.

A Linux bridge will be created in the host and will be NATTed to the main
Internet egress device on the host to give connectivity to the created VMs,
which should also be able to connect among them. Default network range for the
created VMs is `172.26.0.0/24`.

This is not a proper software project, it's just for demonstrating purposes.
Modify scripts at your convenience. It has only been tested with `bionic`
[Ubuntu cloud images](https://cloud-images.ubuntu.com/), but in principle should
work with minor modifications with any Linux cloud image that supports
initialization via `cloud-init`.
