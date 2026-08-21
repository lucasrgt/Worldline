# M317 behavior map

Packet15 places cobweb item `30` on a raised stone column and soul sand
item `88` on the west face. The actor then Packet13-walks the same
intended step in air, inside that live cobweb cell, and on that soul sand.
Standing pose deltas over eight ticks are slower in cobweb (`250`
milli-blocks) and on soul sand (`400` milli-blocks) than in air (`1000`
milli-blocks). Both cells survive a clean save plus fresh login.

This is not M195 cobweb placement or M192 soul-sand placement. Those only
prove the planted cells. This is distinct from M331 cobweb slow, which
does not claim soul sand. Sword-break cobweb and string drops are not
claimed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobweb30+soulsand88-path|cause=packet15-item30+packet15-item88+packet13-walk|wire=packet13-pose-deltas-8ticks|oracle=cobweb-pose-slower-than-air+soulsand-pose-slower-than-air|column=17,support=4:71:4:1:0,cobweb=4:72:4:30:0,soulsand=3:71:4:88:0,ticks=8,air=1000,web=250,soul=400,slower=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`bcae75456216b2655361256edd97079669619d908394782145a21a076e9e676a`.
