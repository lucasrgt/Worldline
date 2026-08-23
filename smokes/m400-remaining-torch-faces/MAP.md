<!-- worldline-map-schema=1 -->
<!-- boundary=m400-remaining-torch-faces -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ce7b2efbd3293b6dc413e9dd2c1b1c8af938af338cd70f50f3b973772d173868 -->

# M400 behavior map

One official session places remaining wall-torch attachments `50:1`, `50:2`,
`50:3`, and `50:4` on one raised stone column. Packet15 of torch item `50` on
the east, west, south, and north faces writes those four damages. The four
cells survive a clean save plus fresh login.

This map does not re-qualify floor torch `50:5` (M175) or redstone-torch
invert `76:4 -> 75:4` (M312). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+torch50:1+50:2+50:3+50:4|cause=packet15-item50-east+west+south+north|wire=packet53-torch50:1+50:2+50:3+50:4|oracle=remaining-wall-torch-faces+fresh-login|column=17,support=4:71:4:1:0,east=5:71:4:50:1,west=3:71:4:50:2,south=4:71:5:50:3,north=4:71:3:50:4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ce7b2efbd3293b6dc413e9dd2c1b1c8af938af338cd70f50f3b973772d173868`.
