<!-- worldline-map-schema=1 -->
<!-- boundary=m151-netherrack-fire -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=26bb6ad826b35c24a64688c8cc4ded9c503948812eb0c8b6007301c10f10f355 -->

# M151 behavior map

Flint and steel ignites air above official netherrack. The fire block remains
after a bounded live wait and a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87|cause=packet15-flint-and-steel259|wire=packet53-fire51|oracle=live-hold+fresh-login-netherrack-fire|column=17,rack=4:72:4:87:0,fire=4:73:4:51:0,hold=40ticks,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`26bb6ad826b35c24a64688c8cc4ded9c503948812eb0c8b6007301c10f10f355`.
