<!-- worldline-map-schema=1 -->
<!-- boundary=m160-cake-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ec442ce01a43be294030f3c3fb48319afc75e6eb99291a4e24cab8a54f3d8074 -->

# M160 behavior map

Official `BlockCake` (block 92) is placed from cake item 354 and eaten with
empty-hand Packet15. Vanilla cake has six bites stored as metadata `0..5`;
the sixth bite removes the block. One bite requires `health < 20` and restores
three health points, not a hunger bar.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+blockcake92|cause=packet15-item354-place+empty-hand-packet15-bite|wire=packet53-cake92:0->1+packet8-health17->20|oracle=blockcake-one-bite-metadata+health|column=17,support=4:71:4:1:0,cake=4:72:4:92:0->1,health=17->20,heal=3,bites=6,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ec442ce01a43be294030f3c3fb48319afc75e6eb99291a4e24cab8a54f3d8074`.
