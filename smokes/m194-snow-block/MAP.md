# M194 behavior map

Packet15 places snow block item `80` on a raised stone column. The official
server writes snow block `80:0`. That exact cell survives a clean save plus
fresh login. Snow layer `78`, snowfall, and melting are not claimed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+snowblock80|cause=packet15-item80|wire=packet53-snowblock80:0|oracle=live-block80:0+fresh-login|column=17,support=4:71:4:1:0,snowblock=4:72:4:80:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`27b0f59762b6d741c75eb15488c5800f88feb7ca971582557e47532e6cc98a83`.
