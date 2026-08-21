# M441 qualification cycle

`RemainingFoodRestSetCycle` repeats the remaining ItemFood air-use rest
family in two fresh official server JVMs. Each run Packet15-eats cookie
`357` and mushroom stew `282`, then reloads empty cookie slot plus bowl
`281` and health `20`. The signal must include both consume transitions
and the `19 -> 20` and `12 -> 20` Packet8 path. It must not collapse to
M374 remaining-food-eat, M327 crafts, or cake eat. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingFoodRestSetCycle.java m441-remaining-food-rest-set
```

Canonical evidence uses two official server JVMs and three client sessions.
The frozen semantic SHA-256 is
`a742d0481ec2e053071b64ffb13a565582bd3dbbc76859b4d650f2a8b74ac5b7`.
