# M557 qualification cycle

`OneTickPulseSetCycle` rebuilds the 1-tick repeater pulse limiter into
piston `33` in two fresh official server JVMs. Each run places west
repeater `93:3`, then fires one 4-tick lever Packet15 on/off pulse. The
frozen signal includes `pulse=one-tick` and `drop=pushed-block` with
stone retained at the destination and base `33:4`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`cd7816b4b28602a9d7bb4cb6e65bbfc8918216b84e075b8912af314905ec7c05`.

Run directly with:

```text
java tools/smoke/OneTickPulseSetCycle.java m557-one-tick-pulse-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
