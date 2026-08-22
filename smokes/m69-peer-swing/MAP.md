# M69 Named Peer Swing Evidence Map

| Boundary | Exact evidence |
| --- | --- |
| Identity | Packet20 binds the actor username to the peer-visible entity ID |
| Equipment | Packet5 proves the actor holds exact diamond sword ID 276, damage 0 |
| Encoder | Production codec emits packet18 + entity ID int + animation byte 1, six bytes total |
| Request | The local typed result preserves username, login entity ID, and animation 1 |
| Observation | A freshly armed peer tracker accepts only the matching named entity and animation 1 |
| Repetition | Two fresh official-server scenarios produce the same semantic trace signature |
| Lifecycle | Both sessions disconnect cleanly and actor NBT retains one inventory entry |

Packet18 has no target, damage, or health fields and is not an acknowledgment.
M69 does not claim Packet7/38/8, attack acceptance, causal damage attribution,
rendering, Aero timing, repeated swing behavior, or reconnect identity cleanup.

Frozen expected signature SHA-256: `4362b6b5b0cffbbf3429c6cfdad25ff3e077ed5be9a3f7e2f729f3806b9b69b3`
