<!-- worldline-map-schema=1 -->
<!-- boundary=m320-leather-armor-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=48274c2675afd82a6d376e7ec9ceb1e8896adc3761f59461d619a2ae378b90f4 -->

# M320 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts leather helmet `298`, chestplate `299`, leggings `300`, and boots `301`
from leather `334` in the opened 3x3 workbench. Those four result ids remain
in personal storage after a clean save plus fresh login. Armor slots 5-8 stay
empty, so this map is not M270-M273 equipment and not M314 iron crafts.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+workbench58+leather334|cause=packet102-workbench-leather-armor|wire=packet103-result-298-299-300-301|oracle=live-craft+fresh-login+unequipped|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,crafts=298,299,300,301,ingredient=334,equipped=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`48274c2675afd82a6d376e7ec9ceb1e8896adc3761f59461d619a2ae378b90f4`.
