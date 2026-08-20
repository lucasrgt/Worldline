# M306 qualification cycle

`ClosablesCycle` rebuilds the raised stone plus wooden-door plus trapdoor
fixture in two fresh official server JVMs. Each run places door item 324,
opens it, closes it, places trapdoor item 96 against the east face, opens
it, closes it, and reloads both closed cells. The signal must include the
door close `64:4/12 -> 64:0/8` and the trapdoor close `96:7 -> 96:3`. An
open-only result matching M277 or M278 fails. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`0287dd23ec4f04c0960b98f43f8e16ff75d416ad1fb8ffb16478c579b8bc4865`.

Run directly with:

```text
java tools/smoke/ClosablesCycle.java m306-closables
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
