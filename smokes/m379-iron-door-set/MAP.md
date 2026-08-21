# M379 behavior map

Official iron door item 330 is placed onto a raised stone support as lower
`71:0` and upper `71:8`. Lever item 69 is placed against that support's
east face as unpowered `69:1`. Empty-hand Packet15 powers the lever to
`69:9` and opens both door halves to `71:4` / `71:12`. A second empty-hand
Packet15 unpowers the lever to `69:1` and closes both halves to `71:0` /
`71:8`. A clean save plus fresh login retains the closed door and
unpowered lever. This compound is distinct from M241 place-only, M118
redstone open-only, and M306 wooden closables.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+irondoor71+lever69-east|cause=packet15-item330-place+packet15-item69-place+empty-hand-packet15-lever-on+empty-hand-packet15-lever-off|wire=packet53-door71:0/8->4/12->0/8+packet53-lever69:1->9->1|oracle=irondoor-place+redstone-open-close+fresh-login|column=17,support=4:71:4:1:0,lower=4:72:4:71:0->4->0,upper=4:73:4:71:8->12->8,lever=5:71:4:69:1->9->1,persisted=71:0/8+69:1,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9d887adb7cbebcca0c805d02f84507310ea3211b6e1abb774ec7e7ae8d3e4f0c`.
