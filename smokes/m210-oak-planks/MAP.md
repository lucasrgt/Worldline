<!-- worldline-map-schema=1 -->
<!-- boundary=m210-oak-planks -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=00344a185d84b7fb4fd15968e5ef176dc91d8034d17cada76115ac37d3d437f1 -->

# M210 behavior map

Packet15 places wood planks item `5` on a raised stone column. The official
server writes planks `5:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim other wood types as separate plank blocks; Beta
1.7.3 has only wood planks `5`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+planks5|cause=packet15-item5|wire=packet53-planks5:0|oracle=live-block5:0+fresh-login|column=17,support=4:71:4:1:0,planks=4:72:4:5:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`00344a185d84b7fb4fd15968e5ef176dc91d8034d17cada76115ac37d3d437f1`.
