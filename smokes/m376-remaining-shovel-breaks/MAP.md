<!-- worldline-map-schema=1 -->
<!-- boundary=m376-remaining-shovel-breaks -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=40c64c0c07f6bc2b0dd8ed47b2526c1b5ef81a70c4b44720126cc44bc5d15c52 -->

# M376 behavior map

Packet15 places clay `82`, snow `78`, snow block `80`, and soul sand `88`
one at a time on the same raised stone column. Packet14 while holding
gold shovel item `284` fully breaks each cell to air and emits a Packet21
drop.

The signal names those four block ids. Clay drops clay balls `337`. Snow
and snow block drop snowballs `332`. Soul sand drops itself. The cells
are `82:0`, `78:0`, `80:0`, and `88:0` then `0:0`.

This map does not claim farmland `60`, hoe till, grass, shovel durability,
or M302 dirt/sand/gravel. It is distinct from M204/M203/M194/M192 place
persistence.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+clay82+snow78+snowblock80+soulsand88|cause=packet14-goldshovel284|wire=packet53-air+packet21-id337+packet21-id332+packet21-id88|oracle=remaining-shovel-breaks-drops+cells-82-78-80-88-to-0|column=17,support=4:71:4:1:0,clay=4:72:4:82:0->0:0,drop=packet21-337:1:0,snow=4:72:4:78:0->0:0,drop=packet21-332:1:0,snowblock=4:72:4:80:0->0:0,drop=packet21-332:1:0,soulsand=4:72:4:88:0->0:0,drop=packet21-88:1:0,shovel=284,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`40c64c0c07f6bc2b0dd8ed47b2526c1b5ef81a70c4b44720126cc44bc5d15c52`.
