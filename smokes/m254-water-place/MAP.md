<!-- worldline-map-schema=1 -->
<!-- boundary=m254-water-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7349aeca365432ce5e3996f11aa401973978fa83d7fe895578999c7cd306cac2 -->

# M254 behavior map

Water bucket `326` places confined still water `9:0` into a raised stone
basin. Packet15 targets the empty air above the support, including
direction-255 raytrace, so the official `ItemBucket` path can write the
source. Packet103 replaces the selected hotbar stack with empty bucket
`325:1:0`. The still water and empty bucket survive a clean save plus
fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+empty-air|cause=packet15-support+packet15-dir255-bucket326|wire=packet53-water9+packet103-bucket325|oracle=live-place+fresh-login-still-water+empty-bucket|column=17,floor=4:71:4:1:0,water=4:72:4:0:0->9:0,held=326:1:0->325:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7349aeca365432ce5e3996f11aa401973978fa83d7fe895578999c7cd306cac2`.
