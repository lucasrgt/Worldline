# M500-SW qualification cycle

`RainTransitionCycle` creates a fresh vanilla world, stops it cleanly, patches
`level.dat` dry with a bounded `rainTime`, restarts the official server, and
connects a protocol-14 client that proves dimension `0`, arms, and awaits the
fresh `Packet70Bed(1)` in two fresh official server JVMs. After clean
save/stop it asserts the dual-snapshot save-order oracle: `level.dat_old`
carries the Overworld `raining=true` snapshot while the canonical `level.dat`
keeps the dry patched countdown. The frozen signal names
`live=packet70-reason1` and `save-order=overworld-then-secondary`. One
official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RainTransitionCycle.java m500-sw-rain-transition
```

The frozen semantic SHA-256 is
`3a90b1745c24f4ba910f209f1f4939d631063e67acfcee4f610933d55b69eb7d`.

Canonical evidence uses two fresh smoke JVMs, four official server JVMs (one
world creator and one transition server per scenario), and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
