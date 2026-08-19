# M104 qualification cycle

`PairedPagesControlCycle` runs four fresh graphical-client/modded-server arms
as two balanced pairs. Pair one runs pages enabled then disabled; pair two
runs disabled then enabled. Only the plan and nonce are shared inside a pair.

The cycle freezes pages true/false, cache maximum one, rebuild sentinel
negative one, TTL100000, vanilla FPS maximum zero, Aero pacing false, 300
warmup frames/five seconds, and at least720 complete intervals/twelve seconds.
Every aligned census and 60-byte sidecar record is parsed strictly through EOF
and checked against the arm-specific structural state. Worktree provenance,
server Aero exclusion, lifecycle, hashes, clean shutdown, and cleanup are also
required. Diagnostic mode executes one pair but cannot qualify or emit release
evidence.

The canonical arms retained 4847 enabled, 4723 disabled, 4597 disabled, and
4112 enabled complete records. Every record followed its exact arm-specific
path. All timing values and directions remain outside the frozen signature.

The frozen semantic SHA-256 is
`a91f910fbbf2ced951e0a009e1db64924f8b8a33f34aeca4f8b0e6b6e2bc4df8`.
