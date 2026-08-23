<!-- worldline-map-schema=1 -->
<!-- boundary=m145-two-block-piston -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b086d950c86277e5c21762909ed03f11e3a5bd753aa2b5b0aa898edaf9adb88f -->

# M145 behavior map

The raised west-facing piston `33:4` has payload `[stone 1:0, cobblestone 4:0]`
and an empty third cell. One lever activation creates base `33:12`, head
`34:4`, then payload `[stone 1:0, cobblestone 4:0]` shifted one cell west.

The exact payload transitions are `1:0→34:4`, `4:0→1:0`, and `0:0→4:0`.
Together with lever and base they form a five-cell raised digest, subsequently
confirmed through fresh Packet51.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+piston33-west+payload-stone1-cobble4+lever69|settle=200+10ticks|cause=packet15-lever-activate|effect=official-two-block-push|observation=fresh-login-packet51|column=10,lever=5:64:4:1->9,piston=4:65:4:4->12,cells=1:0->34:4/4:0->1:0/0:0->4:0,raised-states=5:54a81608e86fa7a023be273a7038091af2f7b54f8829613b5bc9c94b97742003|disconnect=clean
```

Frozen semantic SHA-256:
`b086d950c86277e5c21762909ed03f11e3a5bd753aa2b5b0aa898edaf9adb88f`.
