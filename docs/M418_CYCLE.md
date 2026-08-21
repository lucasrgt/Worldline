# M418 qualification cycle

`RemainingObsidianPlaceCycle` rebuilds the raised stone fixture in two
fresh official server JVMs. Each run places four obsidian item `49` cells
as an unlit portal-frame fragment, proves portal `90` is absent, then
Packet14-digs the cap cell with diamond pickaxe `278`. The signal must
name multiple `49` cells and pick harvest `49`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`7c15aa18aedb3ac5e34f9b7fbc2836311b51f88fc0737ed40298e3d3e65be80e`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.

Run directly with:

```text
java tools/smoke/RemainingObsidianPlaceCycle.java m418-remaining-obsidian-place
```
