<!-- worldline-map-schema=1 -->
<!-- boundary=m315-dye-wool-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f9b78bfc4331c0fc5e92dd33443743a0c9b46e17815f9f89b20fd2535c2405d2 -->

# M315 behavior map

Eighteen accepted window-0 Packet102 clicks dye white wool `35:0` with rose
red `351:1`, cactus green `351:2`, and lapis `351:4` in the personal 2x2
grid. The official server accepts the predicted colored-wool results
`35:14`, `35:13`, and `35:11`. Those exact stacks survive a clean save plus
fresh login.

This map does not claim wool placement (M197, M248-M287).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=white-wool35:0+dyes351|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-wool-damages+fresh-login|wool=35:0,dyes=351:1+351:2+351:4,results=35:14+35:13+35:11,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f9b78bfc4331c0fc5e92dd33443743a0c9b46e17815f9f89b20fd2535c2405d2`.
