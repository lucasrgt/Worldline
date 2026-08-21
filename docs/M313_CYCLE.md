# M313 qualification cycle

`NoteInstrumentsCycle` rebuilds the raised stone, planks, and sand note-block
fixture in two fresh official server JVMs. Each run places item `25` on three
instrument bases, left-clicks with empty-hand Packet14, captures Packet54
instrument ids `1`, `4`, and `2`, and reloads block `25`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`6e171effe14c350c22319797f836fbb498aa88b559a88bef337aa634f95943b6`.

Run directly with:

```text
java tools/smoke/NoteInstrumentsCycle.java m313-note-instruments
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
