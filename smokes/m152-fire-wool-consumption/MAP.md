<!-- worldline-map-schema=1 -->
<!-- boundary=m152-fire-wool-consumption -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=76938b3c1a673ae193cb53be581ebf52977feb975f5edc7a614a544267681e46 -->

# M152 behavior map

Official netherrack fire consumes adjacent wool. Flint and steel Packet15 on
netherrack `87` places fire `51` in the air cell above. Wool `35` sits in a
face-adjacent cell (`abilityToCatchFire=60`). A bounded 1200-tick live wait
plus a fresh login proves the wool cell is no longer wool (`0` or `51`) while
the netherrack fire remains. The frozen signal is categorical and does not
include the random consume delay.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87+adjacent-wool35|cause=packet15-flint-and-steel259|wire=packet53-fire51-consumes-wool35|oracle=bounded-tick-wool-consumption+fresh-login-netherrack-fire|column=17,rack=4:72:4:87:0,fire=4:73:4:51,wool=5:73:4:consumed,netherrack-fire=present,disconnect=clean
```

Frozen semantic SHA-256:
`76938b3c1a673ae193cb53be581ebf52977feb975f5edc7a614a544267681e46`.
