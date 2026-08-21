# M304 qualification cycle

`FarmlandSetCycle` rebuilds the raised dirt fixture in two fresh
official server JVMs. Each run hoes one dirt plot `3` to farmland `60`,
jumps and falls onto that cell until the official server writes dirt
`3`, and reloads the same dirt cell. The frozen signal includes both
`3->60` and `60->3`. Headless `B173WireClient` is the only client.
There is no GUI and no Aero path. One official EOF or missed trample is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/FarmlandSetCycle.java m304-farmland-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`ce698c2302ea621590b03877774a82c7ea0a5b085bf5536d28093462ed8c121c`.
