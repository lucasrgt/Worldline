# M599 torch wash set behavior map

The fixture raises an isolated 3x3 stone platform. Floor torch `50:5` is
placed on the west cell. Still water `9:0` is placed on the center cell
with a dirt gate on the east cell. Opening that gate lets water occupy
the torch cell and emit Packet21 torch `50`.

This map does not claim floor-torch placement metadata (M175) or remaining
wall-torch faces `50:1` through `50:4` (M400). Headless `B173WireClient`
protocol-14 only.

Frozen signal:
`column=17,platform=3x3,support=4:71:4:1:0,torch=3:72:4:50:5->9:1,source=4:72:4:9:0,gate=5:72:4:3:0->0:0,drop=packet21-50x1,persisted=true,clients=2,disconnect=clean`

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-3x3-stone+torch50:5+adjacent-water9+dirt-gate|cause=packet15-item9+packet15-item50+packet14-open-gate-flow|wire=packet53-torch50:5->water+packet21-50|oracle=torch-wash-not-place-not-faces|column=17,platform=3x3,support=4:71:4:1:0,torch=3:72:4:50:5->9:1,source=4:72:4:9:0,gate=5:72:4:3:0->0:0,drop=packet21-50x1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d262e072e7c810157ca1604a9cfa36fc4eb2e926588f44cee4bf41676177f38e`.
