# M159 behavior map

Packet15 plants sugar cane item `338` on dirt beside still water `9:0`. Official
`BlockReed` random ticks then grow the stack. The frozen oracle is categorical
height `>= 2` after a bounded wait, plus the same cells after a clean save and
fresh login. Exact wait length and extra height are not hashed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9|cause=packet15-item338-reed|wire=packet53-reed83|oracle=official-random-tick-height>=2+fresh-login|column=17,dirt=4:72:4:3:0,water=5:72:4:9:0,base=4:73:4:83:0,height>=2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`70a49f193de25db52e447675752317f7e567b9817943e16a27eb25a669555d8f`.
