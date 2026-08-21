# M253 behavior map

Packet15 places green wool item `35` damage `13` on a raised stone column.
The official server writes wool `35:13`. That exact cell survives a clean
save plus fresh login. This path is distinct from M197 white wool `35:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:13|cause=packet15-item35:13|wire=packet53-wool35:13|oracle=live-block35:13+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:13,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`dafe191ebebe5c80fb8935d352a73bebfb997614628047215ffde468c90be210`.
