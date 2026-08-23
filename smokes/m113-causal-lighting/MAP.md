<!-- worldline-map-schema=1 -->
<!-- boundary=m113-causal-lighting -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c54effdf42a0dcf7c37c7417e2a35d0abfdc85297b2b47398af1d4d86632c822 -->

# M113 behavior map

The official server remains the only light-engine implementation. Worldline
seeds one legal block stack in the official player NBT, sends Packet16 plus
Packet15, and waits for Packet53 to prove the server accepted glowstone 89 at
`(4,55,4)`. Forty Packet10 heartbeats provide a bounded update window.

The first session's cached Packet53 is not used as lighting evidence because
an incremental block update does not carry light planes. After disconnect and
save, a second session receives a new Packet51. M113 compares every X/Z/Y
block-light and sky-light nibble against the immutable pre-placement snapshot.
Each changed sample contributes `(x,y,z,before,after)` to an ordered SHA-256.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|chunk=0,0|intervention=packet15-glowstone89|confirmation=packet53|settle=40ticks|observation=fresh-login-packet51|target=4:55:4,support=3,block=68:68:0:15:9f3d2b0e7d511d0440d2990b3d80649d66a0b38a9d4c14fb889acd99751021fe,sky=0:0:0:0:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855|disconnect=clean
```

SHA-256: `c54effdf42a0dcf7c37c7417e2a35d0abfdc85297b2b47398af1d4d86632c822`.

This proves one server-authored add-source transition only. It does not expose
or reimplement vanilla's light queue, removal rules, rendering, other sources,
chunks, seeds or dimensions.
