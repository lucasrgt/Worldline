<!-- worldline-map-schema=1 -->
<!-- boundary=m588-tool-shatter-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f1fe9cfe500117ca6ec28bf02fd1afd06a79fd20a1415660e79bbdcb77346a54 -->

# M588 behavior map

The M208 raised stone column hosts cobble `4:0` in one session. Packet14
while holding wooden pickaxe `270` seeded at damage `59` (1 durability
remaining) breaks cobble to air. The held stack is destroyed: the hotbar
slot is empty. An unused full-durability wooden pickaxe `270:0` remains.
The empty hand persists across a clean save plus fresh login.

This map does not re-qualify M352 remaining durability damage of `1`.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cobble4+woodpick270-damage59|cause=packet14-woodpick270-last-use|wire=packet53-air+packet103-empty|oracle=held-stack-shatter+fresh-login|column=17,support=4:71:4:1:0,cobble=4:72:4:4:0->0:0,wood=270:59->empty,control=270:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f1fe9cfe500117ca6ec28bf02fd1afd06a79fd20a1415660e79bbdcb77346a54`.
