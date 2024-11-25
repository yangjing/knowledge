# KVM

## 安装

TODO

## 常用命令

```sh
virsh list
virsh list --all

virsh start vm901
virsh shutdown vm901

virsh edit vm901
virsh destroy vm901

virsh dominfo vm901
virsh dumpxml vm901
```

### 创建虚拟机

```sh
virt-install \
  --virt-type=kvm \
  --name=rocky9-x64 \
  --vcpus=2 \
  --memory=4096 \
  --location=/opt/kvm-iso/Rocky-9.5-x86_64-minimal.iso \
  --disk path=/opt/kvm-disk/rocky9-x64.qcow2,size=100,format=qcow2 \
  --network bridge=br0 \
  --graphics none \
  --extra-args='console=ttyS0'
```

### virt-clone

```sh
virt-clone -o rocky9-x64 -n vm901 -f /opt/kvm-disk/vm901-rocky9.qcow2
virt-clone -o rocky9-x64 -n vm902 -f /opt/kvm-disk/vm902-rocky9.qcow2
virt-clone -o rocky9-x64 -n vm903 -f /opt/kvm-disk/vm903-rocky9.qcow2
```

```sh
virsh start vm901
# virsh console vm901
```

### 动态调整参数

```sh
# 设置CPU，下次重启虚拟机会生效
virsh setvcpus vm901 4 --config

# 动态设置内存
virsh setmem vm901 8388608
# 但不能超过设置的内存最大值
virsh setmaxmem vm901 8388608 --config
```

注：虚拟机重启后才会生效（只重启虚拟机内的操作系统不会生效）：

```sh
virsh shutdown vm902
virsh start vm902
```
