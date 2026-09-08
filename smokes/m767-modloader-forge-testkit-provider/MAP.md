<!-- worldline-map-schema=1 -->
<!-- boundary=legacy-loader-testkit-runtime -->
<!-- nonclaims=block-mutation,inventory-mutation,gui-control,performance-equivalence -->
<!-- frozen-trace=d658516d6de1ee1fbbf97275b6ddfe893ea094dff34fb5812437ec46593d1dd9 -->

# M767 ModLoader and Forge TestKit provider behavior map

M767 binds two stable TestKit runtime IDs to hash-pinned historical clients:
`modloader-b1.7.3` and `forge-b1.7.3`. Both providers use the same neutral
`AutomatedMinecraftRuntime` surface and the same synchronous control protocol.

Every test owns a fresh single-player data directory and Java 8 process. The
loader-owned probe creates the seeded world, seals a Worldline profiler run,
reports player/world state, advances one world tick per command, and requests
the native Minecraft shutdown path.

Frozen signal: `providers=modloader-b1.7.3+forge-b1.7.3,discovery=spi,sessions=4,testkit=4-pass,ticks=4,isolation=fresh-singleplayer-client,profiler=4-sealed-wlpr,shutdown=clean`.

Frozen SHA-256: `d658516d6de1ee1fbbf97275b6ddfe893ea094dff34fb5812437ec46593d1dd9`.

The qualified surface is deliberately read-only except for controlled ticking.
Block and inventory mutation, teleportation, GUI control, multiplayer parity,
and performance equivalence remain outside this milestone.

The opt-in progress HUD adds a diagnostic presentation boundary through
`TestRuntimeSession.showProgress`. `HUD` commands render native text while the
probe is waiting for a tick command. Each acknowledgement must preserve the
controlled tick, world time, player identity, health, selected slot, and pose.
The cycle enables the HUD for all four sessions and requires a native render
marker in each client log, while retaining the frozen game-state signal above.
This does not add gameplay input, GUI querying, or assertion authority to the HUD.
