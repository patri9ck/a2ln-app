# Android 2 Linux Notifications App
**Android 2 Linux Notifications** (**A2LN**) is a way to show your Android notifications on Linux with libnotify. It creates a direct socket connection from your phone to your computer whenever a notification arrives. Both devices must be in the same network.

This repository contains the app part of A2LN.
## Which Host Do I Have to Enter?
You have to give the address of the computer that runs the [A2LN server](https://github.com/patri9ck/a2ln-server) to the app.

It can be found out by using `ip route` in your terminal for example:
```
$ ip route
default via 192.168.178.1 dev wlp39s0 proto dhcp metric 600
192.168.178.0/24 dev wlp39s0 proto kernel scope link src 192.168.178.41 metric 600
```
As you can see, the host address is `192.168.178.41` in this case.
## Notes on Specific Phones
### Huawei
The notification listener might be disabled sometimes because of Huawei's strict energy saving methods. To encounter this problem, go to *Settings* > *Battery* > *App launch*, select *Android 2 Linux Notifications* and enable the bottom two options.
## License
A2LN is licensed under the [GPL3](LICENSE).
