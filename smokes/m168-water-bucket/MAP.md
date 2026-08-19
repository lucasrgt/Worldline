# M168 behavior map

Empty bucket `325` picks up confined still water `9:0` from a raised stone
basin. The source cell becomes air `0:0` and Packet103 replaces the selected
hotbar stack with water bucket `326:1:0`. The empty basin and filled bucket
survive a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+still-water9|cause=packet15-water-cell+packet15-dir255-bucket325|wire=packet53-air0+packet103-bucket326|oracle=live-pickup+fresh-login-empty-basin+water-bucket|column=17,floor=4:71:4:1:0,water=4:72:4:9:0->0:0,held=325:1:0->326:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4ce39f3401e15de5c720a314091f69acf985c459785d211d52f84f4af9e47a7d`.
