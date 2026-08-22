# M268 behavior map

Packet15 of flint-and-steel item `259` on a raised stone column places fire
`51:0` in the air cell above and it survives a bounded live hold. After a clean
save and fresh login the stone support must remain, while the random-ticked
fire cell may validly remain fire or decay to air. This is distinct from M151
netherrack keeping flame and from M152 wool consumption.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+flintsteel259|cause=packet15-item259|wire=packet53-fire51:0|oracle=live-place+fresh-login-support+valid-fire-state|column=17,support=4:71:4:1:0,fire=4:72:4:51:0,reload=fire-or-air,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d9303a82df9a2cf43e607f47ba40e1b65d30f92b3e52819d1135a980a6eafafb`.
