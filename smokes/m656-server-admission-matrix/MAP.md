<!-- worldline-map-schema=1 -->
<!-- boundary=server-entry-policy -->
<!-- nonclaims=bans,ip-bans,online-mode-authentication,exact-rejection-wording,arbitrary-capacity-values,duplicate-login-policy,post-beta-protocols -->
<!-- frozen-trace=db5e065110c25d43b1f4ffa771901bc029d97a485b44c663be6cd75052f30513 -->

# M656 official server entry policy behavior map

The official Beta 1.7.3 dedicated server starts in offline mode with `white-list=true`,
`max-players=1`, and two listed identities. Before any player is connected, a third unlisted
identity is rejected during the protocol-14 login handshake. This empty-server probe isolates
whitelist membership from capacity.

One listed identity then completes login and appears in the official player census. While that
identity occupies the sole slot, the second listed identity is rejected as server-full. Because
both identities are listed, that rejection isolates capacity from whitelist membership. After a
clean stop, the same world restarts with whitelist disabled; the originally unlisted identity
then completes login and appears alone in the census.

Frozen signal:

```text
whitelist=unlisted-rejected+listed-accepted,capacity=listed-overflow-rejected,disabled=unlisted-accepted,max=1,identities=3,disconnect=clean
```

The public `server-entry-policy` boundary is the causal official behavior. It is distinct from
M681's tooling-only normalization of already-proven evidence, and it neither imports nor copies
that structural API. This boundary does not claim bans, IP bans, online-mode authentication,
exact rejection wording, other capacity values, duplicate-login policy, or post-Beta protocols.

Frozen trace SHA-256: `db5e065110c25d43b1f4ffa771901bc029d97a485b44c663be6cd75052f30513`.
