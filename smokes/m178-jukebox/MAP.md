<!-- worldline-map-schema=1 -->
<!-- boundary=m178-jukebox -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=97de107318c1552893e50f28f3483ac127f615e7ae8b5018e70e651c21886a86 -->

# M178 behavior map

Official jukebox item `84` is placed on a raised stone support as block `84:0`.
Gold disc `2256` is then used on that cell. The dedicated server emits Packet61
effect `1005` data `2256`, empties the selected slot, and writes metadata `84:1`.
That exact cell remains after a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+jukebox84|cause=packet15-item84-place+packet15-disc2256|wire=packet61-instrument1005-pitch2256|oracle=official-disc-insert+fresh-login-block84|column=17,support=4:71:4:1:0,jukebox=4:72:4:84:1,disc=2256->empty,play=packet61:1005:2256,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`97de107318c1552893e50f28f3483ac127f615e7ae8b5018e70e651c21886a86`.
