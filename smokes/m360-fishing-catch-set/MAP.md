# M360 behavior map

The fixture clones the M180 raised stone column, then adds a still-water
dock of block `9`. Using fishing rod item `346` in-air (Packet15
direction 255) while looking south into that pool emits official Packet23
type `90`. Reeling while the official hook is catchable emits Packet21
raw fish `349`. Catch RNG is not hashed. Distinct from M180 hook-only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+still-water-dock9|cause=packet15-dir255-rod346+reel|wire=packet23-type90+packet21-349|oracle=hook-then-official-catch|column=17,support=4:71:4:1:0,water=4:72:6:9:0,hook=type90,catch=349,rod=346,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`b81e3dfcba437f67fee01101898bab64442120affa5b0cdb60dc16f69a2549b0`.
