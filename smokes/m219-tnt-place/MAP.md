<!-- worldline-map-schema=1 -->
<!-- boundary=m219-tnt-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4f2b9d0bc3bbd9a35010f5efbb02cf5d5e290dcfb590633b35833a02551912f8 -->

# M219 behavior map

Packet15 places TNT item `46` on a raised stone column. The official
server writes unprimed TNT `46:0`. That exact cell survives a clean
save plus fresh login. This map does not ignite, prime, or explode.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+tnt46|cause=packet15-item46|wire=packet53-tnt46:0|oracle=live-block46:0+fresh-login|column=17,support=4:71:4:1:0,tnt=4:72:4:46:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4f2b9d0bc3bbd9a35010f5efbb02cf5d5e290dcfb590633b35833a02551912f8`.
