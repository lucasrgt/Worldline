<!-- worldline-map-schema=1 -->
<!-- boundary=m218-gravel -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3e2635199e586e6323b8da68ffe023b77a589a27d2262ed9aa0f1dc79e604e06 -->

# M218 behavior map

Packet15 places gravel item `13` on a raised stone column. The official
server writes supported gravel `13:0`. That exact cell survives a clean
save plus fresh login.

This map does not claim falling-gravel physics or flint drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+gravel13|cause=packet15-item13|wire=packet53-gravel13:0|oracle=live-block13:0+fresh-login|column=17,support=4:71:4:1:0,gravel=4:72:4:13:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3e2635199e586e6323b8da68ffe023b77a589a27d2262ed9aa0f1dc79e604e06`.
