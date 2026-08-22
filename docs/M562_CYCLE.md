# M562 qualification cycle

`PortalPairSetCycle` rebuilds an Overworld source frame and a second frame
beside the returned Overworld portal in two fresh official server JVMs. Each
run lights the source, travels `0→-1`, returns, seats the neighbor frame in
the returned portal's same 8:1 cell, and travels `0→-1` again. Both outbound
transitions reuse one generated Nether
portal. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/PortalPairSetCycle.java m562-portal-pair-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The semantic SHA-256 is reconfrozen only by the final serialized qualification.
