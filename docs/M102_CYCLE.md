# M102 qualification cycle

`UnlimitedRebuildSentinelCycle` runs two fresh graphical-client/modded-server
replicas with pinned Aero, shared plan/nonce, strict camera, and an Aero-free
common/server closure.

The client rejects any cache maximum other than one, rebuild sentinel other
than negative one, or TTL other than 100000. Every aligned record must report
M74 direct counters 0/0 and M102 page state pageCalls4, direct0, rebuild4,
cache1, and eviction delta four. The runner reparses the complete census and
page sidecar with exact schema, identity, length, EOF, hashes, lifecycle,
provenance, clean shutdown, and worktree cleanup. Diagnostic mode cannot
qualify or emit evidence.

The canonical replicas retained 4724 and 4586 complete records; every one
followed the exact negative-one unlimited-rebuild path. All descriptive timing
values remain dynamic and outside the release signature.

The frozen semantic SHA-256 is
`852d41f2d1654fd1dc83d0b746fddb4c109d370573fd67b25290361ddaefa75b`.
