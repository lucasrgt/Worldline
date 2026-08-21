# M365 behavior map

The actor seeds compass item `345` into hotbar slot 1, Packet16-selects it,
and keeps that stack in hand on the raised-stone fixture. Official
`level.dat` `SpawnX/Y/Z` is the spawn-point the vanilla compass needle
uses. Packet12 yaw `0` then yaw `180` at the support cell must reverse
that needle (`needleDelta=180`). Packet13 then stands on the east stone
pad so the spawn bearing is computed at a second cell.

World spawn itself is session-authored by the official server and is not
hashed. This map does not craft compass `345` (M325), clock `347`, empty
map `358`, compass GUI, or Nether spin.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+held-compass345|cause=packet16-hold-345+packet12-yaw-0-180+packet13-east-cell|wire=level.dat-SpawnXYZ+packet103-compass345|oracle=spawn-point-needle-two-poses+spawn-bearing-two-cells|column=17,support=4:71:4:1:0,east=5:71:4:1:0,compass=345,held=345,yaw0=0,yaw1=180,needleDelta=180,opposite=true,positions=2,spawn=level.dat,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68`.
