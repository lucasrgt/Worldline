<!-- worldline-map-schema=1 -->
<!-- boundary=m278-trapdoor-toggle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a66063d6e5ac041de1eeb23cf5a56d2fe303a9759694e0dd69ce31347ef8442a -->

# M278 behavior map

Trapdoor item 96 is placed against a raised stone east face as official
closed block `96:3`. Empty-hand Packet15 then sets open bit `4`, so Packet53
reports `96:7`. This closed-to-open metadata is distinct from M163 place.
The open cell survives save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+trapdoor96-east|cause=packet15-item96-place+empty-hand-packet15-open|wire=packet53-trapdoor96:3->7|oracle=live-closed-to-open+fresh-login-open-trapdoor|column=17,support=4:71:4:1:0,trap=5:71:4:96:3->7,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a66063d6e5ac041de1eeb23cf5a56d2fe303a9759694e0dd69ce31347ef8442a`.
