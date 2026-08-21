# M299 qualification cycle

`StoneToolCraftsCycle` rebuilds the raised workbench fixture in two fresh
official server JVMs. Each run crafts the stone-tool family from cobble
`4` and sticks `280` and reloads result IDs `272`, `273`, `274`, `275`,
and `291`. Headless `B173WireClient` is the only client. There is no GUI
and no Aero path. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`c7503bc481ed407a57f6a750986b748f269a4222a4a8a2b9a3e26c5a12557c54`.

Run directly with:

```text
java tools/smoke/StoneToolCraftsCycle.java m299-stone-tool-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
