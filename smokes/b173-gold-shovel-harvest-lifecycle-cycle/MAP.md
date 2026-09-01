<!-- worldline-map-schema=1 -->
<!-- boundary=b173-gold-shovel-harvest-lifecycle-cycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=94ebb0d00a32aff99cfaa946a43166353fd6e8ed6bcece74bf2d865e38fa298c -->

# Beta 1.7.3 gold-shovel harvest lifecycle family

This independently signed family binds six complete public TestKit lifecycles to the unmodified
official Beta 1.7.3 server.

| Row | Subject | Placement/reload/break | Exact drop matrix | Tool state |
| --- | --- | --- | --- | --- |
| sand | `block/012` | `12:0 -> air` across two fresh logins | `12:1:0` | gold shovel `284:1:1` |
| gravel | `block/013` | `13:0 -> air` across two fresh logins | `13:1:0` at the fixed seed | gold shovel `284:1:1` |
| snow layer | `block/078` | `78:0 -> air` across two fresh logins | `332:1:0` | gold shovel `284:1:1` |
| snow block | `block/080` | `80:0 -> air` across two fresh logins | four `332:1:0` entities | gold shovel `284:1:1` |
| clay | `block/082` | `82:0 -> air` across two fresh logins | four `337:1:0` entities | gold shovel `284:1:1` |
| soul sand | `block/088` | `88:0 -> air` across two fresh logins | `88:1:0` | gold shovel `284:1:1` |

The family does not claim minimum hardness, gravel/flint probabilities, snow accumulation,
gravity after unsupported placement, soul-sand collision, or any client render behavior.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=gold-shovel-harvest,rows=6,passed=6,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx12,evidence=9b1b5cd9d35ec83b6b62ddfe25cfe95285be2c9d0be9da5b6109a6c3026e4e69,isolation=6-fresh-worlds`.
