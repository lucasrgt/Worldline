# M166 behavior map

Official note block `25` is placed on a raised stone support and clicked with
empty-hand Packet15. The dedicated server emits Packet54 play-note. Block
metadata stays `0`; pitch is tile-entity state carried on the packet.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+noteblock25|cause=packet15-item25-place+empty-hand-packet15-click|wire=packet54-instrument1-pitch1|oracle=official-note-click+fresh-login-block25|column=17,support=4:71:4:1:0,note=4:72:4:25:0,click=packet54:1:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ef6696d8923a3640502fdd0b5ff70c4945e9d080f7740b484b82d02f2e719228`.
