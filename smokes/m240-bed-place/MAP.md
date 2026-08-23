<!-- worldline-map-schema=1 -->
<!-- boundary=m240-bed-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=366879f4dbd3ab1b199692d6094ad12c0ee76aa41469342f4ee3fba3d74ec59d -->

# M240 behavior map

Official `ItemBed.onItemUse` places block `26` only on face `1` (UP).
Yaw `0` writes foot metadata `0` at the clicked cell and head metadata
`8` one cell south. Both cells must be air and both supports must be
solid cubes. This map does not occupy the bed or skip night.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+item355-block26|cause=packet15-item355+look-0|wire=packet53-bed26:0/8|oracle=place-halves+fresh-login|column=17,support=4:71:4:1:0,foot=4:72:4:26:0,head=4:72:5:26:8,look=0:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`366879f4dbd3ab1b199692d6094ad12c0ee76aa41469342f4ee3fba3d74ec59d`.
