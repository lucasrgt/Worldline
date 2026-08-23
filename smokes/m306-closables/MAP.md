<!-- worldline-map-schema=1 -->
<!-- boundary=m306-closables -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=0287dd23ec4f04c0960b98f43f8e16ff75d416ad1fb8ffb16478c579b8bc4865 -->

# M306 behavior map

Official wooden door item 324 is placed onto a raised stone support as
lower `64:0` and upper `64:8`. Empty-hand Packet15 opens those halves to
`64:4` / `64:12`, then a second empty-hand Packet15 closes them back to
`64:0` / `64:8`. Official trapdoor item 96 is then placed against the same
support's east face as closed `96:3`, opened to `96:7`, and closed back to
`96:3`. A clean save plus fresh login retains both closed states. This
compound is distinct from M277/M278 open-only persistence.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+woodendoor64+trapdoor96-east|cause=packet15-item324-place+empty-hand-packet15-open+empty-hand-packet15-close+packet15-item96-place+empty-hand-packet15-open-then-close|wire=packet53-door64:0/8->4/12->0/8+packet53-trapdoor96:3->7->3|oracle=woodendoor-close+trapdoor-close+fresh-login|column=17,support=4:71:4:1:0,lower=4:72:4:64:0->4->0,upper=4:73:4:64:8->12->8,trap=5:71:4:96:3->7->3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0287dd23ec4f04c0960b98f43f8e16ff75d416ad1fb8ffb16478c579b8bc4865`.
