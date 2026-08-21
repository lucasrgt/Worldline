# M330 qualification cycle

`BedSleepSetCycle` rebuilds the raised stone-and-bed fixture in two fresh
official server JVMs. Each run places item `355`, advances world time to
night through the existing lab console `time set`, occupies the bed through
Packet15, observes Packet17 sleep enter, and leaves/wakes so the actor is
standing again. The frozen signal must include both enter and leave, or the
same wake after a clean save plus fresh login. One official EOF is retried
after a 5 second sleep.

Run directly with:

```text
java tools/smoke/BedSleepSetCycle.java m330-bed-sleep-set
```

The frozen semantic SHA-256 is
`1415f89a64178b9c0135d108239ba04eb9fca293f9d8ee9005347624eb6842af`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
