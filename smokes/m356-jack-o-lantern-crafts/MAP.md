# M356 behavior map

One official personal 2x2 epoch crafts jack-o-lantern `91` from pumpkin
`86` over torch `50`, then places the leftover pumpkin and the crafted
lantern. This compound craft/place pair is distinct from M171 and M190
place-only:

- pumpkin `86` over torch `50` in window-0 slots `1`+`3` yields jack-o-lantern `91`
- leftover pumpkin `86` places as `86:1` from look yaw `-90`
- crafted jack-o-lantern `91` places as `91:1` from the same look

Both placed blocks persist across a clean save plus fresh login. The frozen
signal includes result ids `91` and `86`. This map does not claim snow
golems, iron golems, carving, or pumpkin stem/crop growth.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+pumpkin86x2+torch50|cause=packet102-window0-pumpkin-over-torch+packet15-item86+item91+look-90|wire=result91+packet53-pumpkin86:1+jackolantern91:1|oracle=craft-output+look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,pumpkin=4:72:4:86:1,jackolantern=4:73:4:91:1,look=-90:0,recipe=86+50->91,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b870de18f5f7c2616c607111ea332fc3f4426f8f5a3a82d713703270066ee5b1`.
