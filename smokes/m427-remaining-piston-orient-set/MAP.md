# M427 behavior map

Packet15 of piston item `33` and sticky piston item `29` writes the
remaining look-derived facings that M293/M294 did not freeze. Adjacent
east/west towers plus player-below-eye proximity produce down metadata
`0`. After the actor stands on the raised support, Packet12 yaws `0`,
`180`, `-90`, and `90` write north `2`, south `3`, west `4`, and east
`5` for both ids. All ten cells survive a clean save plus fresh login.

This map does not claim piston extension, retraction, sticky pull,
powered heads, or the already-frozen up facing `33:1` / `29:1`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+piston33-remaining+stickypiston29-remaining|cause=packet15-item33+item29+look-down-stack+look0+look90+look180+look-90|wire=packet53-piston33:0+33:2+33:3+33:4+33:5+stickypiston29:0+29:2+29:3+29:4+29:5|oracle=remaining-place-facings+fresh-login|column=17,support=4:71:4:1:0,piston=5:80:4:33:0+6:72:3:33:2+6:72:5:33:3+7:72:3:33:4+7:72:5:33:5,sticky=3:80:4:29:0+6:72:2:29:2+6:72:6:29:3+7:72:2:29:4+7:72:6:29:5,look=down-stack+0+90+180+-90,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`467d62056ad74b5561c6e6bf67533b1608d7fc66644062154b00b81109e8ad76`.
