<!-- worldline-map-schema=1 -->
<!-- boundary=m762-wall-sign-support-break -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8a45072ecf806340b62954df2f1636258a9a1be57128f8c1d78d760141d659d5 -->

# M762 wall sign support break behavior map

A raised stone column top `1:0` at `4:71:4` receives an attached wall sign
`68:5` on its east face at `5:71:4` when item `323` is used on that side
through Packet15. No text editing occurs: the tile entity keeps its default
empty lines and no Packet130 update is sent. The supported sign persists as
block `68:5` across sustained world observations while the support stays
stone `1:0`.

Packet14 dig statuses then remove the supporting stone cell. Official
`BlockSign` `canBlockStay` fails on the missing attachment and the physics
update pops the sign: Packet53 replaces `68:5` with air at the sign cell,
and Packet21 drops exactly one sign item `323` count `1` damage `0`. Fresh
login Packet51 keeps the popped sign cell air. Only the east-face
attachment is claimed; standing signs `63`, Packet130 text editing, and
other facing metadata are outside this boundary.

This map is distinct from M245 fresh-login Packet130 sign-text persistence,
M350 sign text editing, M429 remaining attach faces, and M739 sugar-cane
substrate invalidation.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-column+wall-sign68:5|cause=packet15-item323-east+packet14-dig-support|wire=packet53-sign68:5->0+packet21-323|oracle=supported-sign-persistent+support-break-pop-air+fresh-login-air|column=17,support=4:71:4:1:0->0:0,sign=5:71:4:68:5->0:0,drops=packet21-323,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8a45072ecf806340b62954df2f1636258a9a1be57128f8c1d78d760141d659d5`.
