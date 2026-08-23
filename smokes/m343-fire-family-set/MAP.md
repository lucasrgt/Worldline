<!-- worldline-map-schema=1 -->
<!-- boundary=m343-fire-family-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b04d10e87e540d454627a3960abbf311c9912ca625d00f3e71af970ea08e77f6 -->

# M343 behavior map

Packet15 of flint-and-steel item `259` on netherrack `87` places fire `51`
in the air cell above. That netherrack flame remains after a bounded live
hold. Adjacent wool `35` sits in a face-adjacent cell
(`abilityToCatchFire=60`). A bounded 1200-tick live wait plus a fresh login
proves the wool cell is no longer wool (`0` or `51`) while the netherrack
fire remains.

The frozen signal includes fire `51`, netherrack persist, and wool consume.
This family is distinct from shipping M268 stone ignition, M151 netherrack
persist, and M152 wool consumption 1:1. Exact consume delay is not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87+flintsteel259+adjacent-wool35|cause=packet15-item259|wire=packet53-fire51+netherrack-persist+wool35-consumed|oracle=live-fire51+netherrack-hold+bounded-tick-wool-consumption+fresh-login|column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,fire=4:73:4:51,wool=5:73:4:consumed,netherrack-persist=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b04d10e87e540d454627a3960abbf311c9912ca625d00f3e71af970ea08e77f6`.
