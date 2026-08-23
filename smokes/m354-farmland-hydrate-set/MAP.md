<!-- worldline-map-schema=1 -->
<!-- boundary=m354-farmland-hydrate-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=31e18ca11dc6928034468d2a503769a4559f5757e60dffc22e8bf85af35522d2 -->

# M354 behavior map

A wooden hoe tills official dirt into farmland `60`. Four water-adjacent
plots hydrate to `60:7` on a random tick. One isolated dry plot under a
rain roof stays `60:0`. Wheat `59` holds that dry cell against reversion.
The isolated plot is tilled and planted first; water is then installed before
the four hydrated plots are tilled.
The frozen signal includes both `dry=60:0` and `hydrated=60:7`. Both
states survive a clean save plus fresh login. This is distinct from M156
hydration-only and M304 till/trample.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9+isolated-dry-dirt3+wheat59-hold|cause=packet15-wooden-hoe290|wire=packet53-farmland60:0+farmland60:7|oracle=live-ticks+fresh-login-dry-60:0+hydrated-60:7|column=17,support=4:71:4:1:0,dry=4:72:10:60:0,plots=4,water=5:72:4:9:0,hoe=290,dry=60:0,hydrated=60:7,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`31e18ca11dc6928034468d2a503769a4559f5757e60dffc22e8bf85af35522d2`.
