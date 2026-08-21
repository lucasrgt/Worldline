# M173 behavior map

Packet15 places two adjacent fence items `85` on a raised stone column.
Both cells become block `85:0`. Those exact cells survive a clean save
plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+adjacent-fence85|cause=packet15-item85|wire=packet53-fence85:0|oracle=adjacent-placement+fresh-login|column=17,support=4:71:4:1:0,west=4:72:4:85:0,east=5:72:4:85:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`87da22ea0cb364e70239d8989a677f835f86ebbe9768dee6955f04ee4be1f74e`.
