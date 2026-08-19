# M189 behavior map

Packet15 places bookshelf item `47` on a raised stone column. The official
server writes block `47:0`. That exact cell survives a clean save plus
fresh login. Enchanting is not claimed; Beta 1.7.3 has no enchanting table.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+bookshelf47|cause=packet15-item47|wire=packet53-bookshelf47:0|oracle=bookshelf-metadata+fresh-login|column=17,support=4:71:4:1:0,bookshelf=4:72:4:47:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`30ee0483e42551b855a4fd5a85002dc5871168bc8f2bed26ec7dcd572b2b97a3`.
