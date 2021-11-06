# Android 2 Linux Notifications App
**Android 2 Linux Notifications** is a way to show your Android phone notifications on your Linux desktop using libnotify.

It creates a direct socket connection to your PC whenever a notification arrives. **Therefore your phone and your PC must be in the same network.**
## Which Address Do I Have to Enter?
When you start the [A2LN server](https://github.com/patri9ck/a2ln-server), it outputs the address: `Address: 192.168.178.41:6000`.

Alternatively, you can find it out by using `ip route` in your terminal for example:
```
$ ip route
default via 192.168.178.1 dev wlp39s0 proto dhcp metric 600
192.168.178.0/24 dev wlp39s0 proto kernel scope link src 192.168.178.41 metric 600
```
With both methods, we can see that the address is `192.168.178.41` in this case.
## Notes on Specific Phones
### Huawei
The notification listener might be disabled sometimes because of Huawei's strict energy saving methods. To encounter this problem, go to *Settings* > *Battery* > *App launch*, select *Android 2 Linux Notifications* and enable the bottom two options.
## License
A2LN is licensed under the [GPL3](LICENSE).
