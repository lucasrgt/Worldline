<!-- worldline-map-schema=1 -->
<!-- boundary=m175-torch -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1b9a95028d397daf378283e42e4657f27df1e1e761003ef151f0fdd1790c3c3d -->

# M175 behavior map

Packet15 places torch item `50` on a raised stone column. The official
server writes floor-torch `50:5`. That exact cell survives a clean save
plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+torch50|cause=packet15-item50|wire=packet53-torch50:5|oracle=floor-metadata+fresh-login|column=17,support=4:71:4:1:0,torch=4:72:4:50:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1b9a95028d397daf378283e42e4657f27df1e1e761003ef151f0fdd1790c3c3d`.
