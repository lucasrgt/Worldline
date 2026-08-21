# M402 behavior map

One official session places detector rail item `28` on a raised stone column as
unpowered `28:0`. Packet15 of minecart item `328` on that detector emits
Packet23 type `10` with thrower `0` and a quantized pose at the detector
center. Occupancy bit 8 then writes `28:8`. Both unpowered and occupied states
are in the same cycle. That occupied cell survives a clean save plus fresh
login.

This map does not re-qualify unpowered place-only `28:0` (M185), powered-rail
torch power `27:8` (M309), or powered-rail launch onto a detector (M377). It
does not place regular rail `66`, boat type `1`, chest cart type `11`, or
furnace cart type `12`. Headless `B173WireClient` only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+detector28+minecart328|cause=packet15-item28+packet15-minecart328|wire=packet23-type10+thrower0+packet53-detector28:0->8|oracle=unpowered-then-occupied-detector+fresh-login|column=17,support=4:71:4:1:0,detector=4:72:4:28:0->8,cart=type10+thrower0+fixed144:2331:144,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`00ed23852b2822be0b8b8766debc5cf5049c7e54b7c106f0e7c8d6a5028b8ab3`.
