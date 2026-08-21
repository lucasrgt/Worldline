# M308 qualification cycle

`FragileSetCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run Packet14-breaks ice `79` and glass `20`, places
floor torch `50:5` beside a second ice, and waits a bounded official-tick
window until that ice is still water `9:0`. One official EOF is retried
after a 5 second sleep. Exact melt delay is not hashed.

The frozen semantic SHA-256 is
`016e31ada167a1772c3c0ec4d610d946ddf26bc0a93c97ad494019ab72c97ce5`.

Run directly with:

```text
java tools/smoke/FragileSetCycle.java m308-fragile-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
