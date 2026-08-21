# M268 behavior map

Packet15 of flint-and-steel item `259` on a raised stone column places fire
`51:0` in the air cell above. That exact fire cell survives a bounded live
hold and a clean save plus fresh login. This is distinct from M151 netherrack
keeping flame and from M152 wool consumption.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+flintsteel259|cause=packet15-item259|wire=packet53-fire51:0|oracle=live-place+fresh-login-official-fire-or-decay|column=17,support=4:71:4:1:0,fire=4:72:4:51:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e73f7f6c77c41d2facb9ca438c3905559515101c6dcdbe3cfd22c4b48da0aeda`.
