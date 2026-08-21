# M425 qualification cycle

`RemainingMachineFacesCycle` rebuilds the raised 3x3 stone fixture in two
fresh official server JVMs. Each run places remaining look-yaw facings of
dispenser `23`, furnace `61`, and pumpkin `86`, then reloads those nine
cells after save plus fresh login. The signal must include `23`, `61`, and
`86` plus multiple remaining facing metas, and must not claim place-only
`23:3`, `61:2`, or `86:1`. One official EOF is retried after a 5 second
sleep. Headless `B173WireClient` is the only client. There is no GUI and
no Aero path.

Run directly with:

```text
java tools/smoke/RemainingMachineFacesCycle.java m425-remaining-machine-faces
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`5f5f8026b3aef5768a963db53d9393ac9ed86b766118d805407d5cf5b11a5dbf`.
