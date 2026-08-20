# M425 behavior map

One official session places remaining look-yaw facings of dispenser `23`,
furnace `61`, and pumpkin `86` on a raised 3x3 stone fixture. Packet12 yaw
`0`, `90`, and `-90` write dispenser `23:2`, `23:5`, and `23:4`. Yaw `90`,
`180`, and `-90` write furnace `61:5`, `61:3`, and `61:4`. Yaw `0`, `90`,
and `180` write pumpkin `86:2`, `86:3`, and `86:0`. All nine cells survive a
clean save plus fresh login.

This map does not re-qualify the shipping 1:1 place-only traces (M231
`23:3` look `180`, M221 `61:2`, M171 `86:1` look `-90`) or dispenser eject
(M333). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+dispenser23+furnace61+pumpkin86|cause=packet15-item23+look0+look90+look-90+packet15-item61+look90+look180+look-90+packet15-item86+look0+look90+look180|wire=packet53-dispenser23:2+23:5+23:4+furnace61:5+61:3+61:4+pumpkin86:2+86:3+86:0|oracle=remaining-look-facing-metadata-set+fresh-login|column=17,support=4:71:4:1:0,disp=4:72:4:23:2+5:72:4:23:5+6:72:4:23:4,furnace=4:72:5:61:5+5:72:5:61:3+6:72:5:61:4,pumpkin=4:72:6:86:2+5:72:6:86:3+6:72:6:86:0,look=0+90+-90+90+180+-90+0+90+180,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`5f5f8026b3aef5768a963db53d9393ac9ed86b766118d805407d5cf5b11a5dbf`.
