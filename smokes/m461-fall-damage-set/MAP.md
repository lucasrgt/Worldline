<!-- worldline-map-schema=1 -->
<!-- boundary=m461-fall-damage-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=15947f02196bef6a87d9ec502c93150c37c209cdfeae766857f8f345c1b76cf4 -->

# M461 behavior map

A raised-stone column plus east pad hosts two official ungrounded Packet13
walk-offs. The 6-block drop freezes Packet8 `20 -> 17`. Golden apple `322`
restores health, then a 10-block drop freezes Packet8 `20 -> 13`. Packet38
status 2 accompanies each hurt. Health 13 survives a clean save plus fresh
login.

This map does not re-qualify M307 drowning/suffocation/lava or M469 void
death. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+east-pad+drop6+drop10+golden-apple322|cause=packet13-ungrounded-walk-off+packet15-item322|wire=packet38-status2+packet8-health20->17/20->13|oracle=fall-damage-set-taller-hurts-more+fresh-login|column=18,support=4:72:4:1:0,pad=5:64:4:1:0,short=6:20->17,tall=10:20->13,heal=322,status=2,taller=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`15947f02196bef6a87d9ec502c93150c37c209cdfeae766857f8f345c1b76cf4`.
