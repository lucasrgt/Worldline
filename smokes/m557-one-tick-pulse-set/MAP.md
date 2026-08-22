# M557 behavior map

A raised west-facing piston-`33` arm is driven by a 1-tick repeater pulse
limiter rather than a side-lever hold. Repeater item `356` is placed as
unpowered `93:3` (facing west, look yaw `90`) on the east pad. A floor
lever sits one cell further east. One 4-tick Packet15 on/off pulse through
that diode extends piston `33` then immediately retracts.

Stone is left in the pushed cell (`2:65:4:1:0`) with head air and base
`33:4`. That is a dropped/pushed block, not an M144 sticky pull and not
an M367 full lever hold.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=repeater-pulse-limiter+piston33-west|settle=200+4+20ticks|cause=packet15-lever-one-tick-pulse|effect=official-one-tick-pulse+piston33-drop|observation=fresh-login-packet51|column=10,pulse=one-tick,drop=pushed-block,piston=4:65:4:33:4,head=3:65:4:0:0,pushed=2:65:4:1:0,lever=6:65:4:69:off->on->off,repeater=5:65:4:93:3,delay=1,facing=3,look=90:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`cd7816b4b28602a9d7bb4cb6e65bbfc8918216b84e075b8912af314905ec7c05`.
