# M355 qualification cycle

`NoteRestInstrumentsCycle` rebuilds the raised glass and gold note-block
fixture in two fresh official server JVMs. Each run places item `25` on those
remaining instrument bases, left-clicks with empty-hand Packet14, captures
Packet54 instrument ids `3` and `0` (distinct from M313's `1`, `4`, and `2`),
and reloads block `25`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`0b8bfa875138db6748a105c9ca98ad10bd8f4ff277dbe49e5d1d96e5790cf868`.

Run directly with:

```text
java tools/smoke/NoteRestInstrumentsCycle.java m355-note-rest-instruments
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
