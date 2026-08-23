<!-- worldline-map-schema=1 -->
<!-- boundary=m303-crop-harvests -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=33bca9f328ddb3c028b792f70233157d997e260e28d47d0115069be6bcba67f0 -->

# M303 behavior map

One raised stone column carries three harvest families. Wooden hoe `290`
tills dirt into farmland `60`. Seeds item `295` plant wheat `59:0`. Packet15
bone meal `351:15` writes that crop to `59:7`. Packet14 then breaks the
mature cell to air and emits Packet21 wheat `296:1:0`.

Item `338` plants reed `83:0` on dirt beside still water `9:0`. Packet14
breaks that cane to air and emits Packet21 `338:1:0`.

Sand `12:0` two cells south of the wheat plot receives cactus item `81` as
block `81:0`. Packet14 breaks that cactus to air and emits Packet21 `81:1:0`.
Sand remains. Health stays 20.

This map does not wait for random-tick wheat growth or cane height. Seed
drops `295` are the planted item, not a hashed Packet21 lottery.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-wheat59+reed83+cactus81|cause=packet15-seeds295+bonemeal351:15+packet14|wire=packet53-air+packet21-296+338+81|oracle=crop-harvest-drops|column=17,wheat=4:73:4:59:0->59:7->0:0,seeds=295,cane=5:73:5:83:0->0:0,cactus=4:73:6:81:0->0:0,drops=packet21-296:1:0+338:1:0+81:1:0,hoe=290,bonemeal=351:15,farmland=60,health=20,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`33bca9f328ddb3c028b792f70233157d997e260e28d47d0115069be6bcba67f0`.
