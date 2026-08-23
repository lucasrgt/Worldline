# M156 behavior map

A wooden hoe tills official dirt into farmland after adjacent still water is
already present, preventing a dry random tick from reverting the fixture.
Water raises farmland metadata to the hydrated value on a random tick. At least
one of four raised plots remains `60:7` after a bounded live wait and a
clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9|cause=packet15-wooden-hoe290|wire=packet53-farmland60|oracle=live-ticks+fresh-login-farmland60:7|column=17,plots=4,water=5:72:4:9:0,hoe=290,hydrated=7,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`fec7ee0f7082dd84e4b7dfdfb08bfecf7258e0369cad355481d6c673a7bebb3f`.
