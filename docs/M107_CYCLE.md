# M107 qualification cycle

`PairedSkipIndividualCycle` runs two balanced fresh-process pairs: skip true
then false, followed by false then true. A pair reuses only its plan and nonce.
Minimum2, pages=true, cache=-1, rebuilds=-1, TTL100000, FPS maximum0, Aero
pacing false, 300 warmup frames/five seconds, and at least720 complete
intervals/twelve seconds are frozen and validated.

The M74 census and unified 60-byte sidecar are parsed through exact EOF and
cross-checked by index. Skip true requires queue16/rendererCalls0. Skip false
requires queue16/rendererCalls16. Both require flush2/pageCalls4/direct0,
rebuild0/cache4/eviction0/immediate0, all sixteen identities, M74 render/list
0/0, strict X/Y/Z, fixed test camera, Aero-free server closure, clean shutdown,
and worktree cleanup. Diagnostic mode cannot qualify.

The diagnostic pair retained 4964 skip-true and 4920 skip-false records. The
canonical arms retained 750 skip-true, 4626 skip-false, 4838 skip-false, and
5233 skip-true records. These counts are dynamic evidence and are not part of
the release signature. Every retained record met its exact structural state;
all timing values remain descriptive and outside qualification.

The frozen semantic SHA-256 is
`913fff54f216f47e06d3886f94f4682b83c1d5bbf49648991c28926d71e8c6f3`.
