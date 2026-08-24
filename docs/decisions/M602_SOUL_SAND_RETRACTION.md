# M602 soul-sand Packet13 retraction

M602 was reserved for a server-observable soul-sand slowdown boundary. The only
draft was preserved at commits `28783ec4b36abe9629000e001c5708fa42540461`
and `6bd1559224b7e97862f1e027d525bd8d1b0fa69a` before qualification.

The canonical milestone command was executed on the clean latter commit:

```text
java tools/harness/Gate.java --milestone m602-soul-sand-slow-set
```

The static phase passed. The official Beta 1.7.3 server then accepted equal
Packet13 displacement on dirt, grass, and soul sand:

```text
protocol-14 soul sand Packet13 was not slower than dirt/grass
1000/1000/1000 unchallenged/unchallenged/unchallenged
```

The complete smoke log has SHA-256
`ffe9c6d32627d6a2a31ea238ac4b5db7ee704616cc8c47e214ac329ffbd7fd4f`.
No PASS receipt was produced or pinned.

The `soul-sand-slow` behavior token is therefore retracted from M602. This does
not retract the client-side slowdown behavior already covered by M317; it only
rejects the duplicate claim that the protocol-14 server applies that slowdown
to a submitted Packet13 step.
