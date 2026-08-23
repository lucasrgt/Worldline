# M179 behavior map

A wooden hoe tills official dirt into farmland after adjacent still water is
already present. At least one raised plot hydrates to `60:7`. Seeds item `295` then plant wheat `59:0`
on those moist cells. The newly planted crop remains `59:0` after a short live
hold and a clean save plus fresh login.

This map does not wait for random-tick growth to `59:7`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9|cause=packet15-wooden-hoe290+seeds295|wire=packet53-farmland60+crops59:0|oracle=live-hold+fresh-login-wheat59:0|column=17,plots=4,water=5:72:4:9:0,hoe=290,seeds=295,wheat=59:0,hydrated=7,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`00d861629497b91621c26cc02b6ec8d56763ad9b4f365028fd10188e36694be8`.
