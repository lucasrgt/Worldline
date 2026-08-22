# M562 behavior map

One official Overworld frame is built and traversed `0→-1`. After cooldown
return, a second east-facing frame is seated in the same 8:1 cell as the
landed return portal. The second interior also emits Packet9 `0→-1` and exits
through the same generated Nether portal. Nearby portal `90` stays six
cells. Exact Overworld pair coordinates may reuse the source or a generated
return frame; they do not enter the frozen signal.

This map does not re-qualify M133 one-way travel, M134 single-portal
roundtrip, M560 scale arithmetic, or M561 distant search. Headless
`B173WireClient` only.

Frozen trace:

```text
pending final serialized qualification
```

The semantic SHA-256 is reconfrozen only by the final serialized qualification.
