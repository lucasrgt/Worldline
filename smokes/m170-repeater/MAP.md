<!-- worldline-map-schema=1 -->
<!-- boundary=m170-repeater -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6c15d889fbdd2c03553d0456cd4206acca9913147855898da285810b5cffe59b -->

# M170 behavior map

Repeater item 356 is placed on a raised west-facing stone line as unpowered
block `93:3`. Packet53 confirms facing metadata from look yaw `90`. A floor
lever on the east input cell is flipped on. After the official 1-tick delay
settles, a fresh Packet51 login must contain powered block `94:3` while the
lever remains on. Flipping the lever off and saving again restores unpowered
`93:3` for a third login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-west-line+repeater93+lever69|cause=packet15-item356-place+empty-hand-packet15-lever|wire=packet53-repeater93:3+packet51-repeater94:3->93:3|oracle=1-tick-hold+fresh-login-unpowered-93|column=17,support=4:71:4:1:0,repeater=4:72:4:93:3->94:3->93:3,lever=5:72:4:0->1,facing=3,delay=1,look=90:0,persisted=93:3,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`6c15d889fbdd2c03553d0456cd4206acca9913147855898da285810b5cffe59b`.
