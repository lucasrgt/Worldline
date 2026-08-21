# M461 qualification cycle

`FallDamageSetCycle` rebuilds the raised-stone plus east-pad fixture in two
fresh official server JVMs. Each run walks off a 6-block drop, freezes
Packet8 `20 -> 17`, heals with golden apple `322`, then walks off a 10-block
drop so Packet8 `20 -> 13` is strictly more damage. One official EOF is
retried after a 5 second sleep.

The frozen signal must name the short and tall Packet8 drops, `heal=322`,
and `taller=true`. It must not collapse to M307 drowning/suffocation/lava
or M469 void death.

Run directly with:

```text
java tools/smoke/FallDamageSetCycle.java m461-fall-damage-set
```

The frozen semantic SHA-256 is
`15947f02196bef6a87d9ec502c93150c37c209cdfeae766857f8f345c1b76cf4`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
