# M154 behavior map

A boat item is used in-air (Packet15 direction 255) while standing in one
natural water cell. Two peers decode the same protocol-14 Packet23 boat
(type 1).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=natural-water9|cause=packet15-dir255-boat333|wire=packet23-type1|oracle=two-peer-identical-boat-spawn|water=4:60:4:9:0,boat=type1+shared-id+packet23,pose=144:1993:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`5da646e53a2e386476060c80fb7c8bce2d187f93133cb6adb76ac439e48439a6`.
