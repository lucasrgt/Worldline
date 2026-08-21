# M341 qualification cycle

`RepeaterDelaySetCycle` rebuilds the raised west-facing repeater line in two
fresh official server JVMs. Each run places item `356` as `93:3` and
Packet15-tunes delay bits through `93:7`, `93:11`, and `93:15`. The signal
must include multiple delay values and must not collapse to M170's 1-tick
pulse. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`5dfcac91e31b99f9d578961c42075eb4456a7e3dde14bf19c6d069bf7dc49136`.

Run directly with:

```text
java tools/smoke/RepeaterDelaySetCycle.java m341-repeater-delay-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
