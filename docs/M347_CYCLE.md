# M347 qualification cycle

`GoldDiamondHoesCycle` rebuilds the raised workbench fixture in two
fresh official server JVMs. Each run crafts gold hoe `294` and diamond
hoe `293` from gold ingots `266`, diamonds `264`, and sticks `280`. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`db8eed6611d61cb0063b32dcf170a0fd66d46799102196c45721842544e6515b`.

Run directly with:

```text
java tools/smoke/GoldDiamondHoesCycle.java m347-gold-diamond-hoes
```

Canonical evidence uses two official server JVMs and two client sessions.
