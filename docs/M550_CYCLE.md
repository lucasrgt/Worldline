# M550 qualification cycle

`DispenserQcSetCycle` rebuilds the raised west-facing dispenser QC fixture
in two fresh official server JVMs. Each run places dispenser `23`, loads
cobblestone `4` through the Trap window, powers the stone above with a
floor lever, and awaits Packet21. The support-east cell stays stone, not
a side lever. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/DispenserQcSetCycle.java m550-dispenser-qc-set
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero. The frozen semantic SHA-256 is
`fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.
