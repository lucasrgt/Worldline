<!-- worldline-map-schema=1 -->
<!-- boundary=m322-diamond-armor-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b54acc14a0bba483871701ba342becc842fe45291b56aca8212a1b71a2b5269d -->

# M322 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts diamond helmet `310`, chestplate `311`, leggings `312`, and boots
`313` from diamonds `264` in the opened 3x3 workbench. Those four result ids
remain in personal storage after a clean save plus fresh login. Armor slots
5-8 stay empty, so this map is not M272 diamond-leggings equipment and not
the M314 iron-ingot crafts.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+workbench58+diamond264|cause=packet102-workbench-diamond-armor|wire=packet103-result-310-311-312-313|oracle=live-craft+fresh-login+unequipped|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,crafts=310,311,312,313,ingredient=264,equipped=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b54acc14a0bba483871701ba342becc842fe45291b56aca8212a1b71a2b5269d`.
