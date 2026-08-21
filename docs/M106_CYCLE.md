# M106 qualification cycle

`PairedMinimumInstancesCycle` runs two balanced fresh-process pairs: minimum2
then minimum5, followed by minimum5 then minimum2. A pair reuses only its plan
and nonce. Pages=true, cache=-1, rebuilds=-1, TTL100000, FPS maximum0, Aero
pacing false, 300 warmup frames/five seconds, and at least720 complete
intervals/twelve seconds are frozen and validated.

The M74 census and unified 60-byte sidecar are parsed through exact EOF and
cross-checked by index. Minimum2 requires queue16/pageCalls4/direct0/cache4 and
M74 render/list0/0. Minimum5 requires queue16/pageCalls2/direct4/cache2 and M74
render/list4/4. Both require two flushes, rebuild0, eviction0, immediate0, all
sixteen identities, strict X/Y/Z, fixed test camera, Aero-free server closure,
clean shutdown, and worktree cleanup. Diagnostic mode cannot qualify.

The canonical arms retained 1885 minimum2, 4481 minimum5, 4547 minimum5, and
5061 minimum2 complete records. Every record met its exact structural state.
All timing values remain outside the release signature.

The frozen semantic SHA-256 is
`f3b298b76961b50be8e4695957f53c7ee1e735d394d0b26886e8c5164553adae`.
