# M404 qualification cycle

`RemainingCartBreakCycle` rebuilds the raised rail-pair fixture in two
fresh official server JVMs. Each run places rail `66` and minecart item
`328`, observes Packet23 type `10`, then places a second isolated rail and
chest-minecart item `342` as Packet23 type `11`. Diamond sword `276`
Packet7-attacks (`leftClick=1`) both objects until Packet21 `328` and
Packet21 `328`+`54`. The signal must include both cart types and both drop
families. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingCartBreakCycle.java m404-remaining-cart-break
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`8a80558c9383a317d0d6a8f145c940ff21cb07ffb3649aa4c564214adde79bcf`.
