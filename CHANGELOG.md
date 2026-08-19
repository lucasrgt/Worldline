# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

## 1.141.0 - M153 Dispenser Eject

Status: GO for one official dispenser item eject.

- Opened Packet100 Trap window (type 3, 9+36 slots).
- Loaded cobblestone via accepted Packet102.
- Powered a west-facing dispenser `23:4` with a side lever.
- Observed Packet21 cobblestone eject and an empty dispenser slot.

The frozen M153 semantic SHA-256 is
`e9ac098cef87b90c28a3fdc264de812fb712489a228157a47e92fa23c958d3ec`.

## 1.140.0 - M152 Fire Wool Consumption

Status: GO for official wool consumption beside a netherrack flame.

- Ignited netherrack fire `51` with flint and steel.
- Placed wool `35` face-adjacent to that fire.
- After a bounded 1200-tick wait, a fresh login proved the wool cell consumed.
- Netherrack fire remained. Delay and air-versus-fire remainder are not hashed.

The frozen M152 semantic SHA-256 is
`76938b3c1a673ae193cb53be581ebf52977feb975f5edc7a614a544267681e46`.

## 1.139.0 - M151 Netherrack Fire

Status: GO for one official netherrack flame that survives a live hold and restart.

- Ignited air above netherrack `87:0` with flint and steel Packet15.
- Held fire `51:0` for 40 ticks on the live cache.
- Reloaded the same netherrack/fire pair after a clean save.
- Repeated the complete fixture in two official server JVMs.

The frozen M151 semantic SHA-256 is
`26bb6ad826b35c24a64688c8cc4ded9c503948812eb0c8b6007301c10f10f355`.

## 1.138.0 - M150 Pig Pork Drop

Status: GO for one official two-peer porkchop drop after adapter-owned death.

- Added adapter-owned observed movement, Packet7 attack and death waits.
- Routed Packet38/29 through one shared entity-event reader.
- Required a prior horizontal AI transition and at least one Packet21 pork `319`.
- Repeated the complete fixture in two official server JVMs.

The frozen M150 semantic SHA-256 is
`90cf54607ffd52b403765121c14d821e80e9996702f158c29efe63aee15b0d33`.

## 1.137.0 - M149 Pig Death

Status: GO for one official two-peer pig death transition.

- Added immutable Packet38/Packet29 `RemoteMobDeath` evidence.
- Added Packet7 diamond-sword mob attack on the existing animal-enabled profile.
- Correlated one spawned pig's death exactly across two peers.
- Repeated the complete death fixture in two official server JVMs.

The frozen M149 semantic SHA-256 is
`c189244beb58382402de4313f9d6be75c90f398e404a7df2ebbbdfa8b34c5048`.

## 1.136.0 - M148 Pig AI Movement

Status: GO for one official two-peer pig movement transition.

- Added immutable fixed-point Packet31/33/34 mob movement evidence.
- Added an opt-in animal-enabled official-server profile without changing the
  existing default profile.
- Correlated one spawned pig's horizontal movement exactly across two peers.
- Repeated the complete AI fixture in two official server JVMs.

The frozen M148 semantic SHA-256 is
`c265a9aa7d1e6254b11458774346f05613c82569948443803f4742740e933397`.

## 1.135.0 - M147 Piston Push Limit

Status: GO for the exact official twelve-versus-thirteen push boundary.

- Built matched vertical chains with alternating stone and cobblestone.
- Proved twelve blocks move through thirteen exact chain transitions.
- Proved thirteen blocks leave the piston and every payload cell invariant.
- Repeated both arms across four fresh official server JVMs.

The frozen M147 semantic SHA-256 is
`6fd354f14bc191c11fd670b0d58e6aa0b86072feec3bb2322261cef951ca1a54`.

## 1.134.0 - M146 Obsidian Piston Rejection

Status: GO for one official immovable obsidian payload.

- Built a powered normal piston against exact obsidian `49:0` and air.
- Proved piston, payload and destination remain invariant after activation.
- Required the one-cell raised digest containing only the lever transition.
- Repeated the complete fixture in two official server JVMs.

The frozen M146 semantic SHA-256 is
`5deacfe1aa98b05c6667cd13215354e232659bd057f50e1340640017dface768`.

## 1.133.0 - M145 Two-Block Piston

Status: GO for one official two-block piston chain.

- Built a normal piston with distinct stone/cobblestone payload cells.
- Observed all three exact payload transitions without identity ambiguity.
- Required the five-cell raised digest and fresh-session final states.
- Repeated the complete fixture in two official server JVMs.

The frozen M145 semantic SHA-256 is
`b086d950c86277e5c21762909ed03f11e3a5bd753aa2b5b0aa898edaf9adb88f`.

## 1.132.0 - M144 Sticky Piston Pull

Status: GO for one official sticky-piston pullback.

- Rebuilt M143's geometry with sticky piston `29` and independently reloaded
  its exact extended state.
- Deactivated the lever and observed head `34:12→stone 1:0` plus displaced
  stone `1:0→air`.
- Froze the four exact raised transitions that distinguish sticky pullback
  from normal-piston retention.
- Repeated the three-session lifecycle in two official server JVMs.

The frozen M144 semantic SHA-256 is
`a56517b95b971f64b951329f03267a3c65259a557dc00925e24e3f9235fe377a`.

## 1.131.0 - M143 Piston Retraction

Status: GO for one official normal-piston retraction.

- Rebuilt and extended the exact M142 fixture, then qualified it through a
  clean save and fresh client before treatment.
- Deactivated the lever and observed base `33:12→33:4` plus head `34:4→air`.
- Proved the non-sticky invariant that the displaced stone remains in its new
  cell while exactly three raised fixture states change.
- Repeated the complete three-session lifecycle in two official server JVMs.

The frozen M143 semantic SHA-256 is
`ed36c9824aa5c765b651fa5a53fa268e5427568f47fabaeb082ec26f7639e2e1`.

## 1.130.0 - M142 Piston Extension

Status: GO for one official lever-powered piston displacement.

- Built a west-facing normal piston with one stone in front and a side lever.
- Observed exact base `33:4→33:12`, stone-to-head `1:0→34:4`, and
  air-to-displaced-stone `0:0→1:0` transitions.
- Scoped the immutable delta to the raised fixture so generated water beneath
  the artificial tower is not falsely attributed.
- Reproduced the exact four-cell delta and fresh-session persisted states in
  two official server JVMs.

The frozen M142 semantic SHA-256 is
`48c199a75f4cb6d77ffd1cfec3081c5fa9880915553d5b7e913ddc7cb6a38a20`.

## 1.129.0 - M141 Pig Spawner Observation

Status: GO for one official living-entity creation and Packet24 observation.

- Added immutable `RemoteMobSpawn` and cumulative `MobObservationSession`.
- Added a bounded Packet24 queue and strict protocol-14 metadata decoder.
- Built a raised grass platform whose official default spawner creates pig
  type `90` inside the documented random volume.
- Correlated the identical positive entity identity and metadata across two
  simultaneous clients in two fresh official server JVMs.

The frozen M141 semantic SHA-256 is
`a148241c4e0282a64cf461ef362991e001cc17b1c7b06bd12e3f7b5b555fd522`.

## 1.128.0 - M140 Bonemeal Tree Growth

Status: GO for one official player-triggered vegetation generation boundary.

- Built a raised dry dirt fixture with exact oak sapling `6:0`.
- Applied bonemeal `351:15` through Packet15 and observed root log `17:0`.
- Required a persisted bounded-positive trunk and canopy while leaving exact
  randomized tree geometry server-authoritative.
- Reproduced the normalized root and structural evidence in two fresh official
  server JVMs.

The frozen M140 semantic SHA-256 is
`d5bca5667d5f93503d8c2226bf52d4e49d9395d51c2e2da675497b7d6a57d896`.

## 1.127.0 - M139 Water-Lava Reaction

Status: GO for one bounded official fluid-material reaction.

- Built a raised stone-confined basin with still lava `11:0` beside exact air.
- Placed still water `9:0` and observed vanilla neighbor processing convert the
  adjacent lava source to obsidian `49:0`.
- Scoped the causal hash to the two declared cells and reproduced both exact
  deltas in two fresh official server JVMs.
- Proved the water and obsidian states again after clean save and fresh login.

The frozen M139 semantic SHA-256 is
`1ba936e8c311e4af488c393c17f5f68031f6fbb2c7a8b4ae2831985900fcd326`.

## 1.126.0 - M138 Horizontal Lava

Status: GO for one bounded official scheduled lava transition.

- Built a raised two-cell stone trench with still source `11:0` and dirt gate.
- Removed the gate through Packet14 and observed exact Packet53 air.
- Waited through the slower vanilla schedule and observed target `11:2` while
  retaining source `11:0`.
- Scoped the causal hash to source/target so unrelated random ticks are not
  falsely attributed, then proved both states through a fresh client.

The frozen M138 semantic SHA-256 is
`f1d5832ac76c05b0cc786b294c8f29126f9d0a668c6326ca2ecae17b2824a760`.

## 1.125.0 - M137 TNT Explosion

Status: GO for one official isolated TNT blast and Packet60 observation.

- Added immutable `RemoteExplosion` evidence and `ExplosionSession`.
- Decoded the exact protocol-14 Packet60 layout, including its lack of later
  motion fields, and applied listed destroyed cells to the remote cache.
- Ignited TNT `46` with flint and steel, observed strength `4` and required the
  randomized blast list to contain the constructed stone support.
- Proved live support/TNT removal and the same air states after save and a
  fresh client login.

The frozen M137 semantic SHA-256 is
`bb96106b407266a1f02f9e9e8097e71f5d11de9337293e8cf063277cd00f07ed`.

## 1.124.0 - M136 Nether Death Respawn

Status: GO for one official Nether-death to Overworld-respawn lifecycle.

- Generalized `RemoteRespawn` evidence to retain source and destination
  dimensions while preserving its same-dimension constructor.
- Froze the production Packet9 Nether request as `09-ff`.
- Proved a skyless netherrack source view, signed nonpositive death health and
  authoritative Packet9 `-1→0` plus Packet8 health `20`.
- Proved dimension-change cache invalidation, Overworld-only replacement
  chunks and persisted `dimension:health = 0:20`.

The frozen M136 semantic SHA-256 is
`48c243301cfa00388490bde784ac80eb7597256aa539b83f1777b841d77148a1`.

## 1.123.0 - M135 Player Respawn

Status: GO for one official void-death and same-dimension respawn lifecycle.

- Added typed live health observation and a bounded `RespawnSession` boundary.
- Froze Packet9 requests for signed dimensions `0` and `-1` through the
  production encoder.
- Accepted vanilla's signed nonpositive overkill health without rewriting the
  packet, then required a fresh same-dimension Packet9 epoch and health `20`.
- Followed the server-selected corrected spawn into a decoded lit chunk and
  proved empty inventory plus persisted health after clean disconnect.

The frozen M135 semantic SHA-256 is
`22275e37f5b927fb38ddbe53bfb3869f752fa11afe00efc1e57d41edca84f81a`.

## 1.122.0 - M134 Nether Portal Roundtrip

Status: GO for one complete official Overworld-Nether-Overworld journey.

- Discovered the generated Nether portal from its decoded six-cell plane.
- Left the portal for a bounded 220-tick cooldown and re-entered it.
- Observed Packet9 `-1→0` and a second old-dimension cache invalidation.
- Proved a six-portal, fourteen-obsidian Overworld structure after return and
  persisted the player in dimension `0`; source reuse remains dynamic.

The frozen M134 semantic SHA-256 is
`c2f903638b1e364b9781c247e61c22c77a28a036212dbe444db5c62498e2a74b`.

## 1.121.0 - M133 Nether Portal Traversal

Status: GO for one official Overworld-to-Nether portal journey.

- Entered the active M132 portal for a bounded official residence interval.
- Observed Packet9 change the typed live session from dimension `0` to `-1`.
- Decoded the destination Nether chunk after old-world cache invalidation.
- Proved the server-generated 14-obsidian, six-portal counterpart and persisted
  the traversed player's Nether dimension.
- Kept the vanilla portal search's varying exact destination coordinate dynamic.

The frozen M133 semantic SHA-256 is
`5c8ac40f2065949243c4a0e77c0ae9f5757aa4d89247915f6878de01cb72ed5d`.

## 1.120.0 - M132 Nether Portal Activation

Status: GO for an official server-authored obsidian portal activation.

- Built a complete `4x5` frame using fourteen accepted Packet15 placements.
- Proved six empty interior cells before a flint-and-steel interaction.
- Observed exactly six portal block `90:0` transitions and retained the full
  frame and active portal through a fresh client session.
- Scoped the causal hash to the frame interior rather than unrelated scheduled
  world changes.

The frozen M132 semantic SHA-256 is
`033c56bdb9ddf8abbd27735158a33d88a6a07e85cb5294a09bde41e7015d6518`.

## 1.119.0 - M131 Dual-Dimension Session

Status: GO for simultaneous typed Overworld and Nether protocol sessions.

- Added the cumulative public `DimensionSession` boundary.
- Preserved Packet1's signed dimension byte and bounded exact-dimension waits.
- Added Packet9 change handling that clears old-dimension chunks while leaving
  redundant same-dimension respawns intact.
- Qualified concurrent dimensions `0` and `-1` with distinct decoded terrain.

The frozen M131 semantic SHA-256 is
`4fbbe9be7e3cd6ab8fbfddd920b11392711702505cfd14044e93128570b457cd`.

## 1.118.1 - M130 Nether Oracle Hardening

Status: GO after separating stable Nether structure from scheduled fluid and
decoration changes.

- Retained exact positional netherrack and bedrock evidence.
- Normalized lava 10/11 and mushroom decoration 39/40 after fresh runs exposed
  legitimate pre-capture variation.
- Refroze the honest structural signature without broadening M130's claim.

The hardened M130 semantic SHA-256 is
`d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8`.

## 1.118.0 - M130 Nether Login

Status: GO for one official dimension-seeded Nether login and chunk decode.

- Added an opt-in `allow-nether=true` server profile without changing defaults.
- Added exact Overworld/Nether player NBT seeding.
- Decoded and structurally hashed the first official Nether chunk and verified
  saved `Dimension=-1` after clean logout; variable lava flow and mushroom
  decoration are explicitly excluded.

The frozen M130 semantic SHA-256 is
`d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8`.

## 1.117.0 - M129 Cross-Chunk Iron Door Recovery

Status: GO for exact recovery of the cross-chunk multiblock consumer.

- Qualified the open door and powered lever through a fresh client.
- Observed exact lever `9→1`, lower-door `4→0` and upper-door `12→8` metadata.
- Proved inverse `2 + 1` deltas and zero residual states against baseline.

The frozen M129 semantic SHA-256 is
`5a5478fd4aea68c69ed892984bc98353e208065f354857a047bac7f38c00cfac`.

## 1.116.0 - M128 Cross-Chunk Iron Door

Status: GO for one cross-chunk redstone source driving a multiblock consumer.

- Placed the lever at global x=16 and the iron door at global x=15.
- Observed exact lever `1→9`, lower-door `0→4` and upper-door `8→12` metadata.
- Froze two state deltas in the door chunk and one in the lever chunk.

The frozen M128 semantic SHA-256 is
`96c3cc2f75a5a3864e6be8991639b6e92c5d89a71ec0a88eb788dfca0c05a3c4`.

## 1.115.0 - M127 Cross-Chunk Redstone Recovery

Status: GO for exact recovery of one redstone signal across a chunk seam.

- Qualified the powered `69:9` lever and `55:15` wire through a fresh client.
- Deactivated the lever once and observed exact `9→1` and `15→0` transitions.
- Proved one inverse delta per chunk and zero residual states against baseline.

The frozen M127 semantic SHA-256 is
`269f3a7043dc7c483f160233c36890ef075faf03e36300801aa5779f06b05aa2`.

## 1.114.0 - M126 Cross-Chunk Redstone

Status: GO for one lever-to-wire signal across a chunk seam.

- Placed lever `(16,64,3)` and wire `(15,65,3)` in adjacent chunks.
- Proved one Packet15 activation changes lever metadata `1` to `9` and wire
  power `0` to `15`.
- Froze exactly one state delta in each complete post-activation chunk snapshot.

The frozen M126 semantic SHA-256 is
`1464edc1c01b62563d3608f6b60b9ba6ee30470dbf16e2111ac2c2cd59e880e5`.

## 1.113.0 - M125 Cross-Chunk Water

Status: GO for one bounded fluid transition across a chunk seam.

- Built a stone trench with source `9:0` at global x=15 and a dirt gate at x=16.
- Opened the sole destination through Packet14 and observed exact water `9:1`.
- Froze an empty source-chunk delta and exact one-state neighboring-chunk delta.

The frozen M125 semantic SHA-256 is
`c876ddf9f8686e16db848fb38977ff02ea8eb97dea05e21b0837be68f83a6217`.

## 1.112.0 - M124 Cross-Chunk Light Recovery

Status: GO for exact recovery of two block-light planes after source removal.

- Captured M123's lit source and neighbor chunks with an independent client.
- Removed the edge glowstone through Packet14 and observed ocean water restore
  exact source state `9:0`.
- Proved all 55/19 light changes reverse and both baseline-to-final delta sets
  are empty.

The frozen M124 semantic SHA-256 is
`60903e4d40e5297e01412eb69996ce5f3e2b641f1898d67f376ff357d016dbce`.

## 1.111.0 - M123 Cross-Chunk Lighting

Status: GO for one causal vanilla block-light transition across a chunk seam.

- Replaced exact edge water `9:0` at global `x=15` with glowstone `89:0`.
- Proved source light `0→15` and neighboring-chunk water light `0→12` through
  fresh complete Packet51 snapshots.
- Froze 55 source-chunk and 19 neighboring-chunk increases while both skylight
  planes remained unchanged.

The frozen M123 semantic SHA-256 is
`7f93c32c82a360dcdc5c546f69838e8fcbc8a221bf8ad2961bd532876608365a`.

## 1.110.0 - M122 Fixed-Seed Region Lighting

Status: GO for exact block-light and sky-light planes across nine chunks.

- Reused M121's 200-heartbeat, save, restart and fresh Packet51 lifecycle.
- Froze 294,912 exact nibbles and sixteen histogram bins independently for
  each vanilla light plane across chunks `(-1,-1)` through `(1,1)`.
- Applied no normalization and kept causal lighting and arbitrary regions
  outside the claim.

The frozen M122 semantic SHA-256 is
`55f946b28a62caf43a7b02b027f13747f5662e315fbf0c8e70f9cca77a189192`.

## 1.109.0 - M121 Fixed-Seed Region

Status: GO for one exact nine-chunk vanilla surface and solid-seam census.

- Loaded chunks `(-1,-1)` through `(1,1)` in two fresh fixed-seed worlds and
  repeated the census after 200 heartbeats, clean save and official restart.
- Froze the identical 128,529-block aggregate, exact 2,304-column surface and
  solid occupancy across all internal chunk seams.
- Preserved divergent interior masks, raw IDs and metadata as diagnostics
  instead of overstating official scheduled/random-tick determinism.

The frozen M121 semantic SHA-256 is
`c2a08f5e7e5ec2b6767afbc4b26409d586f2fd4ca296d199d823abe8b2b73d4f`.

## 1.108.0 - M120 Horizontal Water

Status: GO for one bounded server-authored horizontal fluid transition.

- Built a stone trench with exact source water `9:0` and one dirt-gated exit.
- Used fresh Packet51 baselines before and after treatment to exclude stale
  incremental ocean-water normalization from the causal delta.
- Proved Packet14 opening produces target `9:1` and exactly one full-chunk
  state change while keeping generic fluids and natural bucket use out of scope.

The frozen M120 semantic SHA-256 is
`c0bbf83eadc6fd56c3697b50ed2d653aebc2fd9e132467354a9bcae89a6daa29`.

## 1.107.0 - M119 Falling Sand

Status: GO for one server-authored block-gravity transition.

- Stabilized sand `12:0` above a stone support and removed that support through
  the qualified Packet14 boundary.
- Required transient lower air, then exact lower sand and upper air through
  live Packet53 state and a fresh Packet51.
- Froze exactly two full-chunk changes while keeping generic gravity, falling
  entities, long falls, gravel and collision behavior outside scope.

The frozen M119 semantic SHA-256 is
`ac00ec1900fdfc0489c6e7d4e9621c916411505d522df3c1fc9f3c53a78eb656`.

## 1.106.0 - M118 Redstone Iron Door

Status: GO for one server-authored powered-consumer transition.

- Used official iron-door item 330 to create exact block-71 bottom/top states
  `0/8` above the stabilized column.
- Activated the adjacent side lever and proved `69:1 -> 69:9`, door bottom
  `0 -> 4`, and door top `8 -> 12` through Packet53 and fresh Packet51.
- Froze exactly three full-chunk state changes while keeping generic consumers,
  closing, topology, collision and tick-exact ordering outside scope.

The frozen M118 semantic SHA-256 is
`e2000f240f0dce5e5fe233611cca6053e50b31c57113fd564387a00f527d7573`.

## 1.105.0 - M117 Redstone Wire Depower

Status: GO for one server-authored lever-to-wire recovery result.

- Rebuilt M116's exact official lever/wire fixture and required the powered
  `69:9` / `55:15` precondition before treatment.
- Toggled the same lever off and proved Packet53 plus a fresh Packet51 expose
  `69:1` / `55:0` after ten bounded signal ticks.
- Froze exactly the two reverse full-chunk state deltas while keeping generic
  topology, attenuation, consumers and tick-exact ordering outside scope.

The frozen M117 semantic SHA-256 is
`87c06977c34465cb580ba9a857102c62e6953ede7cfe339c2730fc9673a699fe`.

## 1.104.0 - M116 Redstone Wire Power

Status: GO for one server-authored lever-to-wire propagation result.

- Added selected held-item-on-block use across the positive protocol item-ID
  range while retaining authoritative inventory and cursor/window guards.
- Used redstone dust 331 to create official wire `55:0` on M115's stabilized
  fixture.
- Proved lever activation changes `69:1 -> 69:9` and propagates wire power
  `55:0 -> 55:15` through Packet53 and a fresh Packet51.
- Froze exactly two full-chunk state deltas and kept generic redstone networks,
  attenuation, depowering, consumers and timing outside qualification.

The frozen M116 semantic SHA-256 is
`973fb75a9541e4f8015d8133d7c99779e6c1ab8b6ef095120609e6a6fcab5587`.

## 1.103.0 - M115 Lever Activation

Status: GO for one server-authored redstone-component activation.

- Added a neutral empty-hand `activateBlock(position, face)` multiplayer
  boundary with personal-window/cursor/selected-hand preconditions.
- Built a deterministic ten-stone above-water side-lever fixture, fixed yaw and
  waited 200 ticks to exclude orientation and fluid drift.
- Proved Packet15 activation changes lever `69:1` to `69:9` through Packet53
  and a fresh Packet51, with exactly one full-chunk state delta.
- Kept redstone propagation, circuits, powered consumers and generic block
  interaction outside qualification.

The frozen M115 semantic SHA-256 is
`497b5d743a5693c925d69d71c02528cf2d16a63ad5c477980b916a0d2b45ae34`.

## 1.102.0 - M114 Causal Water Flow

Status: GO for one server-authored downward vanilla-water transition.

- Broke one dirt cell below naturally generated still water through the
  existing Packet14 begin/finish boundary and required Packet53 air first.
- Proved live and fresh-login observations settle the opened cell from air to
  water `9:8` after forty heartbeats.
- Froze one and only one full-chunk state delta across two fresh official
  worlds and four protocol sessions.
- Generalized local hurt tracking to accept valid ordered health decreases
  while preserving caller-supplied expected-health checks.
- Replaced M66's `/give`/drop/fall fixture with exact official-NBT inventories
  and bounded air-position heartbeats; its frozen combat evidence is unchanged.
- Kept generic fluids, lava/mixing, lateral flow, buckets, timing, rendering
  and cross-chunk behavior outside qualification.

The frozen M114 semantic SHA-256 is
`658a1cbfc4555fb57b3cef83375f655232f18b834afe547330fd96e64c8a5e3e`.

## 1.101.0 - M113 Causal Lighting

Status: GO for one server-authored vanilla light-source transition.

- Seeded one exact glowstone stack in official-format player NBT and placed it
  through the existing protocol-14 selected-slot/block-placement boundary.
- Required Packet53 acceptance, forty update heartbeats, clean disconnect/save
  and a fresh Packet51 light-plane observation.
- Froze exactly 68 increased block-light samples, maximum delta 15 and source
  level 15; sky light remained unchanged.
- Kept generic propagation/removal, cross-chunk light, rendering, other
  sources, alternate terrain and dimensions outside qualification.

The frozen M113 semantic SHA-256 is
`c54effdf42a0dcf7c37c7417e2a35d0abfdc85297b2b47398af1d4d86632c822`.

## 1.100.0 - M112 Fixed-Seed Lighting

Status: GO for deterministic vanilla block-light and sky-light snapshots.

- Decoded all 32,768 nibbles from each light plane in absolute chunk `(0,0)`
  across two fresh official worlds.
- Froze exact block-light and sky-light hashes plus complete 0–15 histograms.
- Added a minimal official-format player-NBT seed so fixed-coordinate world
  observations do not depend on Beta 1.7.3's variable spawn search.
- Kept source attribution, light updates, cross-chunk propagation, rendering,
  alternate seeds and dimensions outside qualification.

The frozen M112 semantic SHA-256 is
`f5180dc49e6d6117c501e903ab16b1015a071cedf027e2444168a40109dc0969`.

## 1.99.0 - M111 Fixed-Seed Terrain

Status: GO for deterministic vanilla terrain at one absolute chunk.

- Generated two fresh worlds with the unmodified official Beta 1.7.3 server
  and fixed seed `17320110707`.
- Decoded all 32,768 block IDs in absolute chunk `(0,0)` and proved identical
  full-volume and 256-column top-Y/ID/metadata surface digests.
- Kept the version's variable player spawn, lighting, biomes, other chunks,
  alternate seeds, dimensions and persistence outside qualification.

The frozen M111 semantic SHA-256 is
`1242a03c15a6e0c36adbefb6ca2b89b166ab1b57f5fb20cf6d3f402a0bec50b1`.

## 1.98.0 - M110 Cell Size Ceiling

Status: GO for the raw-thirty-three/explicit-thirty-two clamp comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2,
  skip-individual false, pages enabled, unlimited cache/rebuild, and TTL100000.
- Proved raw thirty-three and raw thirty-two both exposed effective size32.
- Required every retained record in both arms to keep queue16/rendererCalls16,
  flush2, pageCalls1, direct0, cache1, M74 render/list0/0, rebuild0,
  immediate0, and eviction0.
- Kept generic clamping, configuration quality and all timing directions
  outside qualification.

The frozen M110 semantic SHA-256 is
`4061454ff65c9ef06366042094e79fc165c26e91d6f3af2fcd7f04638a180c0e`.

## 1.97.0 - M109 Cell Size Floor

Status: GO for the raw-zero/explicit-one clamp comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2,
  skip-individual false, pages enabled, unlimited cache/rebuild, and TTL100000.
- Proved raw zero and raw one both exposed effective cell size one.
- Required every retained record in both arms to keep queue16/rendererCalls16,
  flush2, pageCalls0, direct16, cache0, M74 render/list16/16, rebuild0,
  immediate0, and eviction0.
- Kept generic clamping, configuration quality and all timing directions
  outside qualification.

The frozen M109 semantic SHA-256 is
`d5ba4fa589d791959dca34158989889ea9d5c29942b6bc44fca7a18bb800a69e`.

## 1.96.0 - M108 Paired Cell Size

Status: GO for the exact size-two/size-eight comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2,
  skip-individual false, pages enabled, unlimited cache/rebuild, and TTL100000.
- Proved the fixed aligned plan produced pageCalls4/cache4 at size two and
  pageCalls1/cache1 at size eight in every retained record.
- Required both arms to keep queue16/rendererCalls16/flush2, direct0, rebuild0,
  M74 render/list0/0, immediate0, and eviction0.
- Kept memory cost, visual equivalence, better-size and all timing directions
  outside qualification.

The frozen M108 semantic SHA-256 is
`7bd2dd0f5f557a19c07eaf9d79978bfbac81aee3ad313df51ac504740b7c303d`.

## 1.95.0 - M107 Paired Skip Individual

Status: GO for the exact skip-true/skip-false comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2, pages enabled,
  unlimited cache/rebuild budget, and TTL100000.
- Proved skip true used sixteen managed pre-dispatch enqueues and zero
  individual renderer calls in every retained record.
- Proved skip false used sixteen manual enqueues through sixteen individual
  renderer calls while retaining the same four-page structural state.
- Required both arms to keep pageCalls4, direct0, rebuild0, cache4, M74
  render/list0/0, immediate0, and eviction0.
- Kept visual equivalence and all timing directions outside qualification.

The frozen M107 semantic SHA-256 is
`913fff54f216f47e06d3886f94f4682b83c1d5bbf49648991c28926d71e8c6f3`.

## 1.94.0 - M106 Paired Minimum Instances

Status: GO for the exact minimum-two/minimum-five comparison.

- Ran two balanced same-plan fresh-process pairs with pages enabled, unlimited
  cache/rebuild budget, and TTL100000.
- Proved minimum2 retained four cached page calls with no direct fallback and
  M74 render/list counters0/0 in every retained record.
- Proved minimum5 retained two cached page calls plus four direct instances and
  M74 render/list counters4/4 in every retained record.
- Fixed only test-client yaw/pitch before readiness while preserving strict
  server-authored X/Y/Z validation, eliminating physical mouse drift.
- Kept all timing directions outside qualification.

The frozen M106 semantic SHA-256 is
`f3b298b76961b50be8e4695957f53c7ee1e735d394d0b26886e8c5164553adae`.

## 1.93.0 - M105 Paired Cache Capacity

Status: GO for the exact cache1/unlimited-capacity comparison.

- Ran two balanced same-plan fresh-process pairs with pages=true,
  rebuilds=-1, and TTL100000.
- Proved capacity1 rebuilt four pages, retained one, and advanced eviction by
  four in every retained record.
- Proved capacity-1 retained four warmed pages with rebuild0 and eviction0 in
  every retained record.
- Added and validated one vanilla stone support block outside the Aero fixture
  so the unchanged strict camera pose cannot race gravity in replayed plans.
- Kept memory cost and all timing directions outside qualification.

The frozen M105 semantic SHA-256 is
`35da2fabb47ef902a2cbd7b92dc976771d9a80179b76322cf1f26edade4e5898`.

## 1.92.0 - M104 Paired Pages Control

Status: GO for the exact balanced pages-enabled/pages-disabled comparison.

- Ran two balanced fresh-process pairs with the same plan and nonce per pair.
- Proved the enabled path had queue16/pageCalls4/rebuild4/cache1 and no
  immediate calls in every record.
- Proved the disabled path had immediateDirect16 and zero page state in every
  record, with aligned M74 render/list16/16.
- Hardened automatic plans to include the camera support column while keeping
  the strict full-pose readiness gate.
- Kept paired timing summaries dynamic and made no causal or relative-
  performance claim.

The frozen M104 semantic SHA-256 is
`a91f910fbbf2ced951e0a009e1db64924f8b8a33f34aeca4f8b0e6b6e2bc4df8`.

## 1.91.0 - M103 Pages-Disabled Immediate Direct

Status: GO for the exact explicit pages-disabled control.

- Froze literal pages=false with cache1, rebuild sentinel -1, and TTL100000.
- Added a primitive sidecar counter on the exact immediate `drawDirect`
  overload, avoiding the later empty-flush reset of public cell counters.
- Proved immediateDirect16 and M74 renderer/list16/16 in every one of
  4021/3673 records while queued/page/rebuild/cache/eviction stayed zero.
- Kept timing values dynamic and made no relative-performance claim.

The frozen M103 semantic SHA-256 is
`7ebb83eada0eccda5dbb38d2610d92b60abe893d2483212710eb463e0aa285c6`.

## 1.90.0 - M102 Unlimited-Rebuild Sentinel

Status: GO for literal negative-one unlimited rebuilding under cache1.

- Froze cache1, TTL100000, rebuild sentinel -1, and the four-page scene.
- Proved every retained record had pageCalls4, direct0, rebuild4, cache1, and
  cumulative capacity evictions advancing by four.
- Independently required M74 direct renderer/list counters 0/0 and all sixteen
  synchronized identities in 4724/4586 complete records.
- Kept descriptive timings dynamic and bounded the mechanism to literal -1,
  the exact fixture, and pinned Aero revision.

The frozen M102 semantic SHA-256 is
`852d41f2d1654fd1dc83d0b746fddb4c109d370573fd67b25290361ddaefa75b`.

## 1.89.0 - M101 Rebuild-Budget-Zero Direct Path

Status: GO for the exact zero-rebuild direct path under cache1.

- Froze cache1, TTL100000, rebuild budget0, and the existing four-page scene.
- Proved every retained record had pageCalls0, directInstances16, rebuild0,
  cache0, and zero cumulative capacity evictions.
- Independently required M74 renderer/list counters 16/16 and all sixteen
  synchronized identities in 4490/4758 complete records.
- Kept descriptive timings dynamic and bounded the mechanism to the exact
  fixture and pinned Aero revision.

The frozen M101 semantic SHA-256 is
`8e0d8ae9c249c8f2967e0ac534c0ee7b7e79ff6a04bd7b407c89dcd2f5e7b0cd`.

## 1.88.0 - M100 Rebuild-Budget-One Fallback

Status: GO for the exact alternating one-rebuild path under cache1.

- Froze cache1, TTL100000, rebuild budget1, and the existing four-page scene.
- Proved strict alternation between `pageCalls2/direct4` and
  `pageCalls1/direct10`, with aligned M74 renderer/list counters.
- Required rebuild1, cache1, and capacity-eviction delta one in every one of
  4771/4892 complete records; mode counts were 2386/2385 and 2446/2446.
- Kept descriptive timings dynamic and bounded the mechanism to the exact
  fixture and pinned page order.

The frozen M100 semantic SHA-256 is
`322cccb6a7643bf79357d81d1c8b3ecf2bc0c7bcad170993ebbb01fc7fa8d76b`.

## 1.87.0 - M99 Rebuild-Budget Fallback

Status: GO for the exact two-page rebuild budget under one-entry cache pressure.

- Froze cache1, TTL100000, rebuild budget2, and the existing four-page scene.
- Proved every retained record had pageCalls2, directInstances4, rebuild2,
  cache1, and capacity-eviction delta two.
- Independently required M74 direct renderer/list counters 4/4 and all sixteen
  synchronized identities in 5003/4223 complete records.
- Kept descriptive timings dynamic and limited the split to this exact sorted
  page topology and membership distribution.

The frozen M99 semantic SHA-256 is
`bc072d0104007b86828550033fb0aa3e84c179aa5caee84dcd22552c3c9a4ce7`.

## 1.86.0 - M98 Configured-Zero Protected Cache Floor

Status: GO for the protected one-page floor under literal max-cache zero.

- Froze `maxCachedPages=0` with TTL 100000 and the exact four-page fixture.
- Proved every retained record nevertheless had cache1, pageCalls4, direct0,
  rebuild4, and capacity-eviction delta four.
- Retained 4133/3991 complete records with zero rebuild3 records.
- Documented that protected-key eviction prevents zero from disabling paging
  or producing an empty cache in this path.

The frozen M98 semantic SHA-256 is
`0da3de05b8d5c493b974e04eaf1767e07f54b087f387badfdaf5dd48b6f1bb31`.

## 1.85.0 - M97 Page-Capacity-One Thrash

Status: GO for exact all-page rebuild/eviction behavior under a one-entry Aero
page cache.

- Froze capacity one with TTL 100000, rebuild budget eight, and the existing
  sixteen-cell/four-page fixture.
- Proved all retained records had cache1, pageCalls4, direct0, rebuild4, and
  cumulative capacity-eviction delta four.
- Retained 5067/4581 complete records with `rebuild3=0`, separating the result
  from M96's capacity-two tie modes.
- Reparsed every M74/M97 record and required clean pinned lifecycle/provenance.

The frozen M97 semantic SHA-256 is
`93c51ccdd98d0abd4e6da174f6ea76d8ca10ddb31cfed965117945473a39c551`.

## 1.84.0 - M96 Page-Capacity-Two Bounded Thrash

Status: GO for bounded rebuild/eviction behavior of the exact four-page scene
under a two-entry Aero page cache.

- Froze capacity two with TTL 100000, rebuild budget eight, and the existing
  sixteen-cell/four-page fixture.
- Accepted only rebuild counts three or four and required each record's
  cumulative eviction delta to equal its rebuild count exactly.
- Observed one 4980-record rebuild3 replica and one 4552-record rebuild4
  replica, exposing pinned JVM/hash tie behavior without fixing mode assignment.
- Preserved four page calls, zero direct fallback, two flushes, and complete
  M74/M96 artifact reconciliation throughout.

The frozen M96 semantic SHA-256 is
`96142417765b773152dc82aba8194765319c2c7bd987d513c5b8b8fd34b89acb`.

## 1.83.0 - M95 Page-Capacity Thrash

Status: GO for the exact four-page fixture under a three-entry Aero page cache.

- Froze cache capacity three, page TTL 100000, rebuild budget eight, and the
  existing fixed sixteen-cell/four-page scene.
- Proved every retained record had cache3, pageCalls4, direct0, rebuild2, and
  exactly two new capacity evictions.
- Bound cumulative evictions and renderer/enqueue/flush spans to aligned
  56-byte records and reparsed every complete M74/M95 record in two replicas.
- Observed 5565/5549 samples with eviction ranges 5..11133 and 5..11101;
  timing values remain descriptive.

The frozen M95 semantic SHA-256 is
`4792da7a14435f7c4abeb761e4b22021b7afe0dc617b33422afba4d087035fa5`.

## 1.82.0 - M94 Default-TTL Page Recovery

Status: GO for default-TTL expiration and reverse recovery of M93's exact
empty six-member page.

- Left both `aero.becell.pageTtlFrames` and `aero.perf.memory` unset, binding
  the pinned normal default of 600 frames.
- Proved one empty-page cache expiration: cached pages `4 -> 3`, expired
  counter `0 -> 1`, and zero max-cache evictions in two fresh replicas.
- Delayed restoration until 30 complete records after the expiry record;
  first member stayed direct/cache3 and second rebuilt/cache4.
- Bound expiry counters plus all twelve transitions to a 184-byte sidecar and
  reparsed every complete M74/M78 record.

The frozen M94 semantic SHA-256 is
`c2617f80713c9054acdf8ade17e4474a3a1ed275a2c092fc6d455363493acfcf`.

## 1.81.0 - M93 Full-Page Depletion Recovery

Status: GO for complete depletion and reverse recovery of one exact pinned
six-member Aero page under a fixed page TTL.

- Removed indices `1,2,3,5,6,7`, then restored `7,6,5,3,2,1` with exact
  ordinal, operation, index, coordinate, nonce, ACK, and state validation.
- Proved membership `16 -> 15 -> 14 -> 13 -> 12 -> 11 -> 10 -> 11 -> 12 ->
  13 -> 14 -> 15 -> 16` across two fresh same-plan replicas.
- Distinguished the batched two-through-six-member route, the direct
  one-member route, and the zero-member route, with symmetric restoration.
- Runtime-gated page TTL at 100000 frames and bound twelve transitions to a
  172-byte sidecar; default-TTL eviction remains a nonclaim.

The frozen M93 semantic SHA-256 is
`f0f506ffa69950d8d4030819a4c6c5ca3f190edcfd3f4ba29f3a4ef4129959ad`.

## 1.80.0 - M92 Third-Member Depletion Recovery

Status: GO for sequential three-cell depletion and reverse recovery inside one
exact pinned six-member Aero page.

- Removed indices one/two/three, then restored three/two/one with exact
  ordinal, operation, index, coordinate, nonce, ACK, and state validation.
- Proved membership `16 -> 15 -> 14 -> 13 -> 14 -> 15 -> 16`; every transition
  retained four page calls, zero direct fallback/render/list, and one rebuild.
- Preserved four cached pages and complete M74 state `0x1010/0xffff`.
- Bound all six transitions to a 100-byte sidecar and reparsed every matching
  M74/M78 record in two fresh same-plan replicas.

The frozen M92 semantic SHA-256 is
`a82e3eb16c9c12a3901e03775d53898a725914562f5bd971d7dc5d2444c75104`.

## 1.79.0 - M91 Larger-Page Depletion Recovery

Status: GO for sequential two-cell depletion and reverse recovery inside one
exact pinned six-member Aero page.

- Removed indices one and two, then restored two and one with exact ordinal,
  operation, index, coordinate, nonce, ACK, and restore-state validation.
- Proved membership `16 -> 15 -> 14 -> 15 -> 16`; every transition retained
  four page calls, zero direct fallback/render/list calls, and one rebuild.
- Preserved four cached pages and complete M74 state `0x1010/0xffff`.
- Bound all four transitions to a 76-byte sidecar and reparsed every matching
  M74/M78 record in two fresh same-plan replicas.

The frozen M91 semantic SHA-256 is
`5f019eb32c7f34b31ca907e9fdbec3b827254a08cdf0cbe11a91c703644b2f7e`.

## 1.78.0 - M90 Larger-Page Sibling Recovery

Status: GO for exact index-two removal/restoration in index one's natural
six-member page under the pinned Aero configuration.

- Targeted `(x,y+2,z)` and derived nonce `root*100+3`, with exact block,
  block-entity, phase, ACK, and restored-state validation.
- Proved membership `16 -> 15 -> 16` while page calls remained four, direct
  fallback/render/list calls remained zero, and both transitions rebuilt once.
- Preserved four cached pages and complete M74 state `0x1010/0xffff` throughout.
- Bound both request/event pairs to the 52-byte sidecar and reparsed every
  corresponding M74/M78 record.

The frozen M90 semantic SHA-256 is
`aac17bb2f371a10cf09b7350c228e000700ac36270dc6d3535e3de74a132a402`.

## 1.77.0 - M89 Sibling-Cell Membership Recovery

Status: GO for exact index-four removal/restoration in index zero's natural
two-member page under the pinned Aero configuration.

- Targeted `(x,y,z+1)` and derived nonce `root*100+5`, with exact block,
  block-entity, phase, ACK, and restored-state validation.
- Proved membership `16 -> 15 -> 16`, page calls `4 -> 3 -> 4`, direct fallback
  `0 -> 1 -> 0`, and one rebuild only at restoration in two fresh replicas.
- Preserved four cached pages and complete M74 state `0x1010/0xffff` throughout.
- Bound both request/event pairs to the 52-byte sidecar and reparsed every
  corresponding M74/M78 record.

The frozen M89 semantic SHA-256 is
`87fa014b6cd31a48c7cffa7f839d0b407ecf823d815a80f1a578afa00828c649`.

## 1.76.0 - M88 Reverse Two-Cell Membership Recovery

Status: GO for reverse-order generation-bound recovery over the two exact M87
cells in the same synchronized Aero fixture.

- Removed/restored index one before index zero while preserving the M87 seed,
  plan, nonce, camera, cache, protocol, spacing, and complete record window.
- Proved membership `16 -> 15 -> 16 -> 15 -> 16` with exact generation/index
  ACK and restore-state binding in two fresh replicas.
- Observed index one rebuild immediately even when first, while index zero used
  the direct fallback even when second; both restores rebuilt once.
- Bound four request/event/index triples to the 76-byte sidecar and reparsed
  every corresponding M74/M78 record.

The frozen M88 semantic SHA-256 is
`986d67c17068113e152c7cec8614bbc518629fff4c27619ec488da6c2548c079`.

## 1.75.0 - M87 Two-Cell Membership Recovery

Status: GO for sequential generation-bound recovery over two distinct cells in
one exact server-authored synchronized Aero fixture.

- Removed and restored cell indices zero and one in separate generations, with
  exact coordinate, derived-nonce, operation, generation, and index binding.
- Proved membership `16 -> 15 -> 16 -> 15 -> 16` while preserving the complete
  M74 state and identity mask in every retained record.
- Observed the first removal use the qualified direct fallback, while the second
  removal rebuilt immediately after the first recovery and did not fall back.
- Bound all four request/event/index triples to a 76-byte sidecar and reparsed
  the corresponding complete M74/M78 records in two fresh replicas.

The frozen M87 semantic SHA-256 is
`091dd5a68a9e7650ef91496f86cbc9dc5e82e006863d097a8e3c637402a103a4`.

## 1.74.0 - M86 Repeated Membership Recovery

Status: GO for two generation-bound remove/restore cycles over the same exact
server-authored synchronized Aero cell.

- Preserved M85's seed, plan, nonce, camera, cache, and recording window while
  adding generations one and two to requests, ACKs, and restore state.
- Proved membership `16 -> 15 -> 16 -> 15 -> 16`, the same three-page plus one
  fallback topology in both removed intervals, and one rebuild at each restore.
- Rejected duplicate, skipped, reordered, cross-generation, wrong-coordinate,
  and wrong-nonce protocol state.
- Bound four request/event pairs to a 60-byte sidecar and reparsed topology from
  the corresponding complete M74/M78 records in two fresh replicas.

The frozen M86 semantic SHA-256 is
`841b311c16d11cbbe669756fd0fc020c4371b650ad9c185d8ab717c7217abc44`.

## 1.73.0 - M85 Natural Membership Recovery

Status: GO for one exact natural remove-then-restore sequence over the same
server-authored synchronized Aero cell.

- Removed cell index zero after retained record 300, with strict block, BE,
  root nonce, derived nonce, server ACK, and client air-block validation.
- Restored the same cell after thirty retained records with a dedicated
  buffered state packet and exact block-entity/nonce convergence.
- Proved membership `16 -> 15 -> 16`, page calls `4 -> 3 -> 4`, direct fallback
  and render/list calls `0 -> 1 -> 0`, and one rebuild only at restoration.
- Bound both request/event pairs to a 52-byte sidecar and complete M74/M78
  records in two fresh same-plan replicas; timing remains descriptive.

The frozen M85 semantic SHA-256 is
`6afe38b10186f67d95eef5d1a1beca81bd168417d7d32d3579dfd654aae0445b`.

## 1.72.0 - M84 Four-Page Topology Contrast

Status: GO for a constant-three-member one-page versus three-page structural
transition over one fixed four-page-key scene.

- Aligned a 4x4 scene across Y/Z page boundaries with populations `9/3/3/1`.
- Removed exact indices `0,1,2` and `0,3,12` in fresh otherwise-equal arms,
  with strict server validation, ACK, and client air-block oracles.
- Proved membership thirteen in both arms and event rebuilds one versus three
  while cached pages/calls remain three and singleton fallback remains one.
- Bound each topology to a 44-byte post-seal artifact and complete M74/M78
  records; additive cost and performance direction remain outside the claim.

The frozen M84 semantic SHA-256 is
`ab9789101de12052aa945af741a37394c4a4b06fb78fa2d3d0737120a45eb39b`.

## 1.71.0 - M83 Page Topology Contrast

Status: GO for constant-two-member same-page versus cross-page structural
transitions over one fixed two-page scene.

- Removed exact indices `0,1` and `0,4` in fresh otherwise-equal arms with
  strict server validation, ACK, and client air-block oracles.
- Proved membership fourteen in both arms, but event rebuilds one versus two,
  while cached pages/calls remain two and fallback remains zero.
- Bound each topology to a 44-byte post-seal artifact and complete M74/M78
  records.
- Kept additive page cost, performance direction, causality, inference, and
  historical lag outside the claim.

The frozen M83 semantic SHA-256 is
`2418e988f23571a72a07c2521eb9ee7cb9ebc8b436957a74d7cf226fe4878f10`.

## 1.70.0 - M82 Natural Wave Ladder

Status: GO for a three-arm structural ladder over one fixed two-page Aero
scene, without a performance or dose-response claim.

- Ran fresh target-count arms 1, 2, and 4 with exact balanced target sets,
  server validation, acknowledgements, and client air-block oracles.
- Proved membership `15/14/12`, event rebuilds `1/2/2`, two cached pages/calls,
  two flush calls, and zero fallback.
- Bound each arm to a 44-byte post-seal sidecar and complete M74/M78 records.
- Kept additive cost, arbitrary topology/cardinality, causality, regression,
  improvement, inference, and historical lag outside the claim.

The frozen M82 semantic SHA-256 is
`2727138a7c9b2eb9e38b7a40a9ae8518a3c3c7b0739c188d2ae152edbbb47bab`.

## 1.69.0 - M81 Natural Multipage Rebuild

Status: GO for one server-authored two-cell change spanning two natural Aero
pages in the synchronized sixteen-identity scene.

- Fixed the same-plan fixture across Z=31/32 and removed exact indices zero and
  eight after retained record 300 with one typed request and acknowledgement.
- Proved both client blocks became air, membership `16 -> 14`, exactly two
  page rebuilds, two cached pages/calls, two flush calls, and zero fallback.
- Cross-bound a 40-byte request/event artifact to complete M74 and M78 records
  across two fresh same-plan/nonce replicas.
- Kept arbitrary topology, additions, repeated/dense waves, causal cost,
  regression, and historical lag outside the claim.

The frozen M81 semantic SHA-256 is
`f30116757d3fcf070289bdb013181744abdaf8da806426cc2efc76128484bc6d`.

## 1.68.0 - M80 Natural Membership Rebuild

Status: GO for one server-authored content removal and the corresponding real
Aero membership rebuild over the synchronized sixteen-identity scene.

- Sent one typed removal request after record 300 and required exact server
  validation, acknowledgement, and an air block at the client coordinate.
- Proved live renderer/enqueue membership `16 -> 15`, one page rebuild, two
  cached pages/calls, two flush calls, and zero direct fallback.
- Cross-bound a 36-byte request/event artifact to complete M74 and M78 records
  across two fresh same-plan/nonce replicas.
- Kept legacy stale block-entity cleanup, generic invalidation, dense waves,
  causal cost, regression, and historical lag outside the claim.

The frozen M80 semantic SHA-256 is
`3df82b51703daacc031e1f745f86fc7af6678d2da74901eb6c00183915e8a77a`.

## 1.67.0 - M79 Cold Page Rebuild

Status: GO for one explicitly armed Aero cell-page cache disposal and rebuild
over the synchronized sixteen-entity scene.

- Preserved the Aero-free common/server class closure while accessing the exact
  renderer model only from a client Mixin.
- Fired once after 300 retained records and required cache `4 -> 0 -> 4`,
  deleted/compiled deltas of four, four rebuilds/calls, and zero fallback.
- Required every other record to remain on M78's warmed rebuild-free path and
  cross-bound a 68-byte cold artifact to the M74/M78 artifacts.
- Ran two fresh same-plan/nonce replicas without an automatic-invalidation,
  causal, regression, or historical-lag verdict.

The frozen M79 semantic SHA-256 is
`94b95453ff0ba5944e7592bbdd8251c064dd0d7aa966cfa2c8b343ce92267d08`.

## 1.66.0 - M78 Paged Stage Timing

Status: GO for real steady-state Aero cell-page enqueue/flush acquisition over
the synchronized sixteen-entity scene.

- Added a client-only Aero marker while retaining an Aero-free common/server
  block-entity class closure.
- Required exact per-record `16` enqueues, `4` cached pages/page calls, zero
  direct fallbacks/rebuilds, and the corresponding M74 structural counters.
- Bound direct renderer/enqueue/flush spans and page counters to every M74
  census index in a post-seal versioned sidecar.
- Ran two fresh same-plan/nonce replicas without a cold-build, causal,
  regression, or historical-lag verdict.

The frozen M78 semantic SHA-256 is
`dbb52fb098cf377aa90027c4000ab7073efa6cbe5bc4f4fa56fa2090d38ae894`.

## 1.65.0 - M77 Direct Stage Timing

Status: GO for index-aligned direct synchronous renderer, Aero enqueue, and
flush spans over the constant sixteen-entity complete census.

- Added preallocated primitive timers around the full renderer, sixteen nested
  `queueAtRest` direct-fallback calls, and two empty-page flush calls per record.
- Wrote a versioned sidecar only after the M74 bracket sealed and cross-bound
  every record to the M74 nonce, plan, count, elapsed time, and fixture state.
- Preserved sub-clock-resolution flush aggregates as zero while requiring a
  positive full series and exact call cardinality.
- Ran two fresh same-plan/nonce replicas and reported descriptive spans without
  an uninstrumented-cost, causal, regression, or historical-lag verdict.
- Clarified that the server-safe plain BE does not implement Aero's paging
  marker, so M77 does not claim cell-page enqueue or populated-flush timing.

The frozen M77 semantic SHA-256 is
`4ac829480cfb8a9409d89c35e002246e43a0a143815303e1ac520e8990988a4c`.

## 1.64.0 - M76 Renderer Decomposition

Status: GO for exact complete-census acquisition across renderer-absent,
renderer-only, and renderer-plus-Aero treatments over one constant scene.

- Removed the renderer registration only after readiness for the `0/0` arm.
- Preserved sixteen renderer calls while independently suppressing or forwarding
  all sixteen nested Aero calls for exact `16/0` and `16/16` records.
- Fixed and runtime-checked vanilla max framerate plus disabled Aero pacing.
- Ran two fresh mirrored triplets and reported mixed descriptive stage deltas
  without a stable-cost, causal, regression, or historical-lag verdict.

The frozen M76 semantic SHA-256 is
`973ae93f8127bae80ceeddc372713f5968213aa1f2fb3a8978c58af61439ac40`.

## 1.63.0 - M75 Aero Exposure Ladder

Status: GO for exact complete-census acquisition at nested Aero call levels
`0/1/4/16` over one constant synchronized scene.

- Held sixteen server-authored block entities, network state, camera, plan, and
  renderer dispatches constant while varying only the Aero queue boundary.
- Required every binary record to contain sixteen dispatches and exactly the
  configured number of real at-rest renders and list calls.
- Ran two fresh mirrored ladders in forward and reverse order.
- Reported level-minus-zero summaries without a monotonicity, dose-response,
  causal, regression, or historical-lag verdict.

The frozen M75 semantic SHA-256 is
`92c9e4e28b17dd1df6750e5aff15022619211a1e981ffb9c3ccea461a3d9da05`.

## 1.62.0 - M74 Complete Aero Census

Status: GO for bounded complete renderer-interval acquisition over the paired
zero-versus-sixteen Aero-content fixture.

- Added a fixed-capacity primitive HEAD-to-HEAD interval recorder after exact
  fixture readiness, without retained per-sample allocation or I/O.
- Disabled the selective Aero logger and reset pinned at-rest counters through
  a test-only invoker at each measured renderer HEAD.
- Wrote one versioned binary artifact only after sealing and reparsed every
  record, aggregate, treatment state, plan, and nonce fail closed.
- Reported whole-census summaries and pair deltas descriptively, without a
  causal, inferential, regression, density, or historical-lag verdict.

The frozen M74 semantic SHA-256 is
`2cc4533688aa06ba1d69309639c36e16688b09eb4deeeb27d044277550d2d1a7`.

## 1.61.0 - M73 Paired Aero Content

Status: GO for balanced absent/present acquisition of sixteen synchronized Aero
content instances.

- Added one exact post-warm-up activation plus tracked-plan readiness handshake
  shared by both paired arms.
- Qualified zero placement/rendering in absent arms and sixteen exact synchronized
  renderer identities in present arms through explicit per-cell content messages.
- Ran two fresh balanced pairs with fixed time/frame windows and async logger files.
- Reported selected-row summaries and mixed pair deltas without an inferential or
  performance verdict.

The frozen M73 semantic SHA-256 is
`41422dda87ca7a8ed192e8c23c9946c55518f87e123cf69d6b1662d689b3b500`.

## 1.60.0 - M72 Aero Server Content

Status: GO for one exact server-authored custom block/entity rendered by Aero.

- Added a server-safe StationAPI content closure with no Aero/client imports.
- Sent distinct server-only state nonces through an explicit M72 content message.
- Bound identifier, raw ID, coordinates, block-entity type, and nonce client-side.
- Invoked the real pinned Aero at-rest renderer and completed twenty later frames.
- Repeated the boundary in two fresh modded server/client/worktree sets.

The frozen M72 semantic SHA-256 is
`6dff186ed904bdce57466038dd32a9824888d6de7ddb1a20041663cb8cec0501`.

## 1.59.0 - M71 Paired Aero Window

Status: GO for balanced paired acquisition and descriptive selected-row summaries.

- Added four fresh matched control/event pairs in balanced order.
- Anchored both arms to one exact Packet3 broadcast observed by the real client.
- Required exact combat absence in control and Packet18-before-Packet38 order in event.
- Captured fixed warmup/window bounds and at least thirty strictly parsed Aero rows per arm.
- Reported per-arm summaries and event-minus-control pair deltas without inferential classification.

The frozen M71 semantic SHA-256 is
`0b26d07ed6b08195a067bf8730b43f49ec596dae274c74f335f8a44576cb1d2b`.

## 1.58.0 - M70 Aero Combat Window

Status: GO for ordered combat-event observation and subsequent Aero frames.

- Composed the M66 combat fixture with the M68 real graphical observer.
- Sent the M69 swing immediately before the M66 attack request.
- Applied named Packet18 before victim Packet38 on the observer stream.
- Captured twenty post-event frames and strictly parsed post-event Aero rows.
- Allowed official reuse of destroyed dropped-item IDs while rejecting live duplicates.

The frozen M70 semantic SHA-256 is
`977bf908fc7edf5e0cf707f81fffaf6208183440a0f07cca81e2b9a22d03e571`.

## 1.57.0 - M69 Named Peer Swing

Status: GO for one isolated Packet18 request and named peer observation.

- Added a cumulative peer-swing session without changing M66 attack semantics.
- Froze the production Packet18 encoder as an exact six-byte message.
- Correlated animation 1 to Packet20 identity after Packet5 sword bootstrap.
- Repeated the official-server boundary and preserved clean persistence.

The frozen M69 semantic SHA-256 is
`4362b6b5b0cffbbf3429c6cfdad25ff3e077ed5be9a3f7e2f729f3806b9b69b3`.

## 1.56.0 - M68 Aero Multiplayer Login

Status: GO for real StationAPI/Aero client and vanilla multiplayer composition.

- Connected a real graphical b1.7.3 client through production `ConnectScreen`.
- Observed Packet1, first Packet13, and applied Packet51 remote readiness.
- Completed twenty post-ready renderer updates with post-ready Aero logs.
- Repeated clean client disconnect/server shutdown from a pinned clean checkout.

The frozen M68 semantic SHA-256 is
`a7978b0bb7e1277d846528036ff3ded3c5541ea5b11bd0935d32580b574e969f`.

## 1.55.0 - M67 Chest Retrieval

Status: GO for exact single-chest retrieval and final-state persistence.

- Added a bounded chest-to-player retrieval contract for exact stone.
- Committed active63, personal45, and cursor state only on matching Packet106.
- Reopened the stored chest before retrieval and closed through M58 proofs.
- Proved empty chest0 and exact personal36 stone after a clean restart.

The frozen M67 semantic SHA-256 is
`cbeb29b97d06faa167bb524366feb7b9d1a92fa03edeb432470d7f1ff0a7b469`.

## 1.54.0 - M66 Player Combat

Status: GO for one bounded armored PvP strike.

- Added username-resolved Packet7 attack requests without exposing raw IDs.
- Correlated a fresh target Packet38 on the attacker stream.
- Ordered victim-local Packet38 before Packet8 health `20 -> 18`.
- Proved diamond-sword wear `0 -> 1` and persisted victim health 18.

The frozen M66 semantic SHA-256 is
`8d05a812d9bfa62ac53321d1cca3f96c2cf9ff76668e36cdf0605945b883022c`.

## 1.53.0 - M65 Peer Armor Equipment

Status: GO for exact leather equipment and named-peer observation.

- Added typed leather moves into personal-window armor slots 5..8.
- Froze all eight Packet102 messages through the production encoder.
- Correlated the reversed armor layout with peer Packet5 slots 4..1.
- Proved Packet104, Packet5 bootstrap, and four NBT entries after restart.

The frozen M65 semantic SHA-256 is
`7bf03514d4331779e14ecaf3379ecf89d3bea276115ca77e909e5a9160587fe4`.

## 1.52.0 - M64 Workbench Output

Status: GO for exact workbench output, consumption, and persistence.

- Confirmed M63's modeled result with accepted slabs44x3:2 prediction.
- Required Packet200 crafted statistic 16842796 with increment three.
- Consumed result/matrix and stored exact slabs in personal slot 36.
- Closed safely and reopened after restart with persistent player/workbench state.

The frozen M64 semantic SHA-256 is
`fa5b92b7450d785451e527f7ecbab2597f99e0b9977b31333541a4e0a155253b`.

## 1.51.0 - M63 Workbench Preparation

Status: GO for bounded three-wide workbench matrix preparation.

- Added adapter-owned left-take and right-place Packet102 actions.
- Correlated the modeled row and cursor count transitions to each accepted ACK.
- Kept pressure-plate/slabs result values explicitly modeled until M64.
- Rejected Packet101 close while workbench result or matrix remains occupied.

The frozen M63 semantic SHA-256 is
`9fd2fb1869b8221cc5e2c9173a548224fb65ca6c6dc9c37858eeb88cd24bf289`.

## 1.50.0 - M62 Workbench Window

Status: GO for typed workbench open/read and safe empty-grid close.

- Preserved Packet100's exact `Crafting` descriptor and declared count of nine.
- Modeled the separate result slot and exact 46-slot Packet104 combined view.
- Reconciled personal slot 36 with combined slot 37 through one layout offset.
- Rejected workbench close unless result, matrix, and cursor are empty.

The frozen M62 semantic SHA-256 is
`975a1e57c412953d693d00c7a6105b5cbdfed428ab8bdc5e58a4ce04dd974fdf`.

## 1.49.0 - M61 Furnace Output Retrieval

Status: GO for exact glass retrieval and restart persistence.

- Continued the M60 container epoch with accepted actions 5 and 6.
- Required the exact glass Packet200 crafted-stat side effect before commit.
- Reconciled output, combined player tail, window 0, and cursor atomically.
- Reopened after restart with personal glass and an empty furnace output.

The frozen M61 semantic SHA-256 is
`3759ec0bd9b8f31341f5c783a82f30592ab69bc97a54da45bd14708f781ff51c`.

## 1.48.0 - M60 Furnace Smelt

Status: GO for exact live furnace loading and smelt observation.

- Added typed Furnace/3/39 remote-window decoding and exact tail reconciliation.
- Loaded sand and coal through four correlated accepted container actions.
- Reconciled asynchronous furnace Packet103 updates into the active window.
- Qualified cook/burn Packet105 progression through exact glass output.

The frozen M60 semantic SHA-256 is
`4d18743104fc8bb5efa84e46268323c5d77af8d121e315b156ea3305cf69b5de`.

## 1.47.0 - M59 Chest Transfer and Restart

Status: GO for accepted player-to-chest transfer and restart persistence.

- Added immutable two-action chest-transfer evidence.
- Reconciled the combined 63-slot view, canonical window 0, and cursor atomically.
- Reset action IDs by window-open epoch rather than reusable numeric ID.
- Reopened a fresh official server process and observed the persisted chest slot.

The frozen M59 semantic SHA-256 is
`4f1bfe9bca33138e8c833162aba2e62e1b120488dac8af034d47b60d10c73c9a`.

## 1.46.0 - M58 Remote Window Lifecycle

Status: GO for explicit remote-window close and confirmed personal restoration.

- Added immutable remote-window closure evidence.
- Sent Packet101 only for the exact locally active window and an empty cursor.
- Confirmed server closure through an accepted no-op Packet102 on window 0.
- Proved later personal transactions, peer held-state, and saved inventory.

The frozen M58 semantic SHA-256 is
`d74f622bc7b86332ec099b367830281038962f547c1a3d80a293a2e56a2ceda4`.

## 1.45.0 - M57 Personal 2x2 Crafting

Status: GO for the bounded personal log-to-planks recipe.

- Added immutable four-action personal crafting evidence.
- Predicted the 2x2 grid/result locally and committed each step only on Packet106 true.
- Reused rejected-transaction recovery for an authoritative empty-grid/planks audit.
- Proved terminal peer-held planks and one saved player-inventory entry.

The frozen M57 semantic SHA-256 is
`a7ca218db3ec5f4fe14ee8f7ec54955d49eb343c9185c62ab6982add0a2e8c7d`.

## 1.44.0 - M56 Rejected Transaction Recovery

Status: GO for rejected personal-click reconciliation and transaction re-enable.

- Added typed immutable rejected-transaction recovery evidence.
- Sent the exact Packet106 true re-enable ACK immediately after Packet106 false.
- Staged Packet104 and cursor Packet103 before atomic authoritative replacement.
- Proved recovery by a subsequent accepted action 2 and one saved inventory entry.

The frozen M56 semantic SHA-256 is
`707a15cd2055ee67795cf2d074d648e4395d644024015ef7ba999fd3c000f85b`.

## 1.43.0 - M55 Accepted Personal Transaction

Status: GO for accepted personal-window left-click transactions.

- Added immutable accepted personal-transaction values and a bounded session API.
- Encoded exact Packet102 action 1/2 predictions for take and place transitions.
- Committed staged slot/cursor state only after matching Packet106 true ACKs.
- Proved server state through peer Packet5 stone/empty/stone and one saved entry.

The frozen M55 semantic SHA-256 is
`c9abcffdd4d7663f0ce225d94bb59f73b07c632512e751f8c403f22ed0e2320e`.

## 1.42.0 - M54 Chest Window

Status: GO for a single-chest descriptor and immutable combined-window read.

- Added neutral immutable remote-window descriptor and container values.
- Corrected Packet100 decoding to its exceptional modified-UTF title format.
- Correlated type 0, title `Chest`, and 27 owned slots with a 63-slot Packet104 view.
- Proved the empty combined view after authoritative chest placement in two fresh worlds.

The frozen M54 semantic SHA-256 is
`c3fe36b177bb6263b467d92726ec430f16fc832f012417a1d5cd20be269a038f`.

## 1.41.0 - M53 Held Block Placement

Status: GO for selected held-block placement with two independent world views.

- Added neutral block faces and a bounded held-block placement session contract.
- Derived the Packet15 stack from the selected authoritative inventory slot.
- Proved Packet53 stone replacement in two immutable remote-world caches.
- Confirmed Packet103/Packet5 consumption and zero clean saved inventory entries.

The frozen M53 semantic SHA-256 is
`3b27d76f04b4e55d0c3197a091a0b98b39a0f9a5fdeee3b34b92f725e91e2472`.

## 1.40.0 - M52 Named Item Collection

Status: GO for exact named collection with terminal removal evidence.

- Added an immutable completed item-collection value and bounded session wait.
- Correlated Packet21 item IDs through Packet22 collector and Packet29 removal.
- Unified local login and remote Packet20 identities in a bounded item coordinator.
- Proved Packet103/Packet5 inventory restoration and one clean saved entry.

The frozen M52 semantic SHA-256 is
`905fe8b02bdc2f81e2280d4658b81440e4d975e6d52ff83a4fd573d0ad8f77af`.

## 1.39.0 - M51 Dropped Item Spawn

Status: GO for immutable dropped-item observation from an independent peer.

- Added a neutral immutable dropped-item value and bounded observation session.
- Strictly decoded Packet21 stack, fixed-point position, and signed velocity.
- Proved the spawn near the dropping actor with non-zero bounded launch motion.
- Retained independent local, peer-held, and clean persistence empty evidence.

The frozen M51 semantic SHA-256 is
`6051025c444760d21cf5a283358b4594612188234b72c7ae363c0a50d907e92f`.

## 1.38.0 - M50 Drop Held Item

Status: GO for drop-current-item with local and independent peer evidence.

- Added an explicit empty held-item value and bounded drop session contract.
- Sent the original Packet14 status-4 drop-current-item action.
- Proved Packet103 local-slot and Packet5 named-peer empty transitions.
- Confirmed zero remaining inventory entries after clean disconnect and save.

The frozen M50 semantic SHA-256 is
`f47c950ee765fa26735061bdf45cbbafbe66a0c8f8251dbd713bcc7c44ec4f3f`.

## 1.37.0 - M49 Held Item Peer Observation

Status: GO for bounded held-slot selection with independent peer evidence.

- Added a neutral held-item value and held-item multiplayer session contract.
- Sent Packet16 only for hotbar indexes 0 through 8.
- Correlated Packet20 named spawns with Packet5 carried-item updates.
- Proved slot-1 dirt selection through a second client on two fresh servers.

The frozen M49 semantic SHA-256 is
`df1873f6f3d7c48c3b34a400cad1a86a6579378b4b25cd5c99d90dcf63453039`.

## 1.36.0 - M48 Server Inventory Observation

Status: GO for bounded server-authoritative inventory observation.

- Added immutable neutral item-stack, indexed-slot, and inventory-window types.
- Decoded Packet104 full windows and applied matching Packet103 slot deltas.
- Proved an empty 45-slot player window followed by a real stone pickup in slot 36.
- Independently confirmed the observed stack in persisted player NBT.

The frozen M48 semantic SHA-256 is
`a501a36c74fa73d37995c8da8050f0718539e38db187539808e6fc491ba55abb`.

## 1.35.0 - M47 Immutable Batch Counts

Status: GO for bounded aggregate batch counts.

- Added immutable completed-route, outcome, and correction counts.
- Computed counts once from immutable route results without replaying events.
- Preserved execution and terminal-event identity.
- Proved exact `2 routes / 3 outcomes / 0 corrections` on two fresh servers.

The frozen M47 semantic SHA-256 is
`5937694a83f953037612da32bd49301d7413eedfe4aab84df98f341cc686bb5f`.

## 1.34.0 - M46 Exact Batch Terminal Event

Status: GO for identity-bound batch terminal summaries.

- Added `EVENT`, `AFTER_ROUTE`, and `EXHAUSTED` terminal kinds.
- Bound every batch result to its exact final indexed correlated event.
- Preserved the M45 result API through delegation to the richer execution.
- Proved all three terminal boundaries across two fresh official servers.

The frozen M46 semantic SHA-256 is
`23e11f826866e54447461ec94740a5e77d76abad7761fabcdf08d0ae5108e521`.

## 1.33.0 - M45 Event-Boundary Batch Stop

Status: GO for batch-wide cancellation at a movement event boundary.

- Added a synchronous batch event controller distinct from after-route control.
- Applied `STOP` immediately after the indexed event and before later movement.
- Proved one resolved outcome, absent later alternative, and absent later plan
  across two fresh official servers.
- Added no rollback, async delivery, parallelism, registry, retry, or adapter change.

The frozen M45 semantic SHA-256 is
`84d799547e96d434049f4879778606a592b3159626bf9df9b7e8225aeb9ca5d6`.

## 1.32.0 - M44 Synchronous Batch Observation

Status: GO for stable-index caller-thread batch observation.

- Added immutable batch events and a non-controlling synchronous observer.
- Indexed routes independently while preserving embedded alternative/outcome
  indexes and caller-owned correlation identity.
- Proved two exhausted routes, exact event order, cache coherence, and final
  persistence across two fresh official servers.
- Added no asynchronous delivery, parallelism, registry, retry, or adapter change.

The frozen M44 semantic SHA-256 is
`67a4fbc25b7288613c49431a9137a7104293d3262d7bd5898cbd0472b516287b`.

## 1.31.0 - M43 Bounded Correlated Route Batch

Status: GO for sequential correlated-route batch control.

- Added immutable correlated route plans and batch results with a 16-plan cap.
- Preserved each route's correlation, terminal event, and termination reason.
- Applied a synchronous batch `STOP` before the next unsent plan.
- Added no parallelism, registry, retry, scheduling, or adapter change.

The frozen M43 semantic SHA-256 is
`3b09e9188cd0948cb17f11f3f203888bfd04845bf599ea20fbd004b1d1a94e44`.

## 1.30.0 - M42 Caller-Owned Route Correlation

Status: GO for identity-preserving opaque route correlation.

- Added correlated event, controller, and execution wrappers around the M41
  route boundary.
- Preserved the exact caller-owned reference by identity in every synchronous
  event and the terminal summary.
- Proved correlated safe movement, terminal stop, and later-movement absence
  across two fresh official servers.
- Added no global registry, serialization, value interpretation, retry,
  scheduling, or adapter change.

The frozen M42 semantic SHA-256 is
`0256ed450183c49365c4ba2475f49203c7f5a1c180caefa5adf017cf87250237`.

## 1.29.0 - M41 Immutable Route Termination

Status: GO for exact stopped-versus-exhausted route summaries.

- Added immutable `MovementRouteExecution` and `MovementRouteTermination`
  values without changing the M39 or M40 entrypoints.
- Bound every summary to its exact final event and identical final outcome.
- Proved `CONTROLLER_STOP` after a fallback and `EXHAUSTED` after a complete
  route across two fresh official servers.
- Preserved later-movement absence, remote cache coherence, and player
  persistence without goal inference, retry, scheduling, or adapter changes.

The frozen M41 semantic SHA-256 is
`f3134a8e626058fc196b5ad3787199c6e0cd7f71a25a8a5db228289b886cdf7a`.

## 1.28.0 - M40 Observer-Directed Route Control

Status: GO for synchronous event-directed route cancellation.

- Added `MovementRouteController` and explicit `CONTINUE`/`STOP` directives
  without changing the M39 observation API.
- Applied each decision immediately after its immutable indexed event and
  before any fallback or later alternative could be sent.
- Proved a corrected primary, accepted fallback, synchronous stop, and absent
  later alternative across two fresh official servers.
- Preserved event/outcome identity, remote cache coherence, and final player
  persistence without an executor, queue, or adapter change.

The frozen M40 semantic SHA-256 is
`6a3285b118eccd8b3f1e95ba51e7f6de46933c168b9f56f2623b11d8d266da7b`.

## 1.27.0 - M39 Synchronous Route Observation

Status: GO for caller-thread indexed route outcome observation.

- Added immutable `MovementRouteEvent`, `MovementAttemptKind`, and a synchronous
  `MovementRouteObserver` boundary.
- Emitted each primary/fallback event immediately after its bounded movement
  resolved, with stable alternative and outcome indexes.
- Preserved object identity between observed outcomes and the immutable final
  route result across two fresh official servers.
- Added no thread or asynchronous game callback; cache and persistence remained
  coherent through the observed route.

The frozen M39 semantic SHA-256 is
`df2973b510807bc1ebce5b49ba2921e14137bd2970fba351d61df46f44165222`.

## 1.26.0 - M38 Explicit Movement Fallback

Status: GO for caller-supplied single fallback after correction.

- Added immutable neutral `MovementAlternative` primary/fallback pairs with a
  bounded 32-pair route entrypoint.
- Skipped fallback after an unchallenged primary and executed exactly one
  fallback after a corrected primary.
- Proved the exact `UNCHALLENGED`, `CORRECTED`, `UNCHALLENGED` outcome sequence
  on two fresh official servers without retrying the blocked primary.
- Preserved the remote cache and persisted the explicit fallback's final pose.

The frozen M38 semantic SHA-256 is
`850b6e29ed5e8aab12e48625ebde6b8ce1902b581d9e07f55c8488f2d7bfd947`.

## 1.25.0 - M37 Route Correction Policy

Status: GO for explicit retry-free stop-on-correction routing.

- Added neutral `RouteCorrectionPolicy` values for continued and stopping
  route execution.
- Preserved M36's continue behavior as the default overload.
- Stopped a three-step route immediately after its second, corrected step on
  two fresh official servers, producing exactly two outcomes and one correction.
- Required zero retry, cache retention, and persisted proof that the third step
  was never applied.

The frozen M37 semantic SHA-256 is
`4a9a43b61c171fd05ab6156b07c963b7c1ebcdedc6ab7ea42d7a40db04cdf649`.

## 1.24.0 - M36 Route Recovery

Status: GO for bounded relative-route continuation after correction.

- Added immutable neutral `MovementStep` and `MovementRouteResult` values plus
  a recovering session contract with a bounded 64-step ceiling.
- Executed route steps relative to the latest resulting pose, including after
  a server-authoritative correction.
- Qualified the exact ordered outcomes `UNCHALLENGED`, `CORRECTED`,
  `UNCHALLENGED` on two fresh official servers.
- Required one correction, preserved the original cache chunk, and persisted
  the final recovered pose after clean disconnect.

The frozen M36 semantic SHA-256 is
`895c39dd8b5e5d0f18c7eac81b76c5da77df74b98ecb434aad93adf49cfbc0c8`.

## 1.23.0 - M35 Bounded Movement Outcome

Status: GO for bounded unchallenged/corrected movement classification.

- Added immutable neutral `MovementOutcome` and `MovementDisposition` values
  plus a resolved sustained-session contract.
- Classified a move as corrected only after an inbound Packet13 was consumed;
  absence during the bounded window remains explicitly `UNCHALLENGED`.
- Persisted a collision-safe `+0.125 X` move in player NBT on two fresh official
  servers, qualifying the live unchallenged evidence as accepted.
- Forced a solid-block collision afterward and required rollback to that last
  accepted pose while retaining the original decoded chunk.

The frozen M35 semantic SHA-256 is
`414c83fa237a0affd1c36ab171e04f07ab110487fc2ebd75698f54e55d92417a`.

## 1.22.0 - M34 Server-authoritative Pose Correction

Status: GO for correction decode, acknowledgement, and neutral pose convergence.

- Decoded server Packet13 with its server-side stance/feet field order and
  rejected invalid stance intervals.
- Acknowledged each correction in the exact client Packet13 field order before
  exposing it to the sustained neutral session.
- Deliberately moved into a solid block selected from the decoded cache on two
  fresh official servers and required exact convergence to the initial pose.
- Preserved the original cached chunk across correction; outbound invalid
  movement alone never counted as success.

The frozen M34 semantic SHA-256 is
`b62641c2a99876737d070566eb1330ab14a569e7e2f7a7ea66293e1e768a302f`.

## 1.21.0 - M33 Chunk Traversal Lifecycle

Status: GO for deliberate cross-chunk movement and rendered cache turnover.

- Rose eight blocks for collision-free clearance, then crossed two eastward
  chunk boundaries in bounded quarter-block movement steps.
- Preserved M30 strict prechunk qualification before movement, then enabled
  bounded implicit edge MapChunk loads observed from the official server.
- Required at least one immutable cached chunk removal and one decoded addition
  after the traversal on each of two fresh servers.
- Rendered before/after cache topologies through mapped Minecraft
  `Tessellator`, native LWJGL, and an offscreen Pbuffer.
- Required a removed chunk pixel to clear, an added chunk pixel to appear, and
  the complete RGBA frame hash to change.

The frozen M33 semantic SHA-256 is
`8f2860494fba146931fbe768d01a5c0dc063d05cc2ac01afd3fa9cce4c8b7e0d`.

## 1.20.0 - M32 Sustained Remote Terrain Render

Status: GO for sustained protocol-14 cache-to-native-render composition.

- Added a neutral 40-tick sustained remote-world session contract.
- Reproduced the unchanged vanilla cadence byte-for-byte: 38 flying packets
  and two periodic pose packets.
- Pumped a multi-chunk decoded view (at least four chunks) while keeping each
  session connected to an unmodified official server.
- Rendered cache-derived 8x8 terrain slices through mapped Minecraft
  `Tessellator`, native LWJGL, and an offscreen Pbuffer.
- Required Packet53 to turn both the exact cached block and its corresponding
  native frame pixel into air before accepting the update.

The frozen M32 semantic SHA-256 is
`7ca1a2fd0d3c4d172e3f123c1b1382a2b939c5ebe0a09e7570acf7a381399f00`.

## 1.19.0 - M31 Incremental Remote World

Status: GO for server-authoritative incremental block updates.

- Added neutral begin/finish break intent and exact expected-block waiting.
- Decoded Packet53 single-block and Packet52 packed multi-block updates into
  immutable replacement snapshots while preserving prior values and light.
- Proved exact mapped coordinate/state application in a deterministic fixture.
- Broke one nearby block in each of two fresh official servers and required the
  inbound target state `0:0`; outbound intent alone never counted as success.
- Bounded exact-state waits by elapsed time even while keepalives arrive, and
  ensured failed server boots terminate their child process.
- Kept mining prediction, drops, full client heartbeat, entities, rendering,
  and server tick stepping as later milestones.

The frozen M31 semantic SHA-256 is
`f238ca0cb8dc430ba88e17dc25425d158569d08d7dc9abda01b97cdc87cde6bf`.

## 1.18.0 - M30 Remote World Cache

Status: GO for bounded prechunk-qualified remote-world caching.

- Added immutable `RemoteWorldView` and `CachedRemoteWorldMultiplayerSession`.
- Unified pose/chat/chunk inbound consumption so native Packet50 load/unload
  lifecycle is retained while other packet types are awaited.
- Required a load reservation before accepting decoded Packet51 data, evicted
  it on unload, and enforced a hard 256-region bound.
- Consumed partial Packet51 regions without caching them, keeping incremental
  range application outside this milestone.
- Proved two-chunk cache semantics in the lifecycle oracle, negative-safe world
  addressing, and one qualified full chunk from each of two official servers.
- Kept incremental block/entity updates, native world construction, rendering,
  and server tick stepping as later milestones.

The frozen M30 semantic SHA-256 is
`efa8065f90fda3c466ccdf7c22d1b54b8a6470fbb61354176467635f3e980631`.

## 1.17.0 - M29 Remote Chunk Snapshot

Status: GO for strict native chunk inflation and neutral block access.

- Added immutable `RemoteChunkSnapshot` and `RemoteWorldMultiplayerSession`.
- Required exact bounded zlib completion and split native payloads into block
  ID, metadata, block-light, and sky-light planes inside the b1.7.3 adapter.
- Compiled mapped vanilla `NibbleArray` from the pinned local workspace and
  compared every coordinate of a synthetic full chunk without vendoring source.
- Decoded real full chunks from two fresh official client/server scenarios.
- Kept prechunk lifecycle, multi-chunk caching, entities, native world
  construction, rendering, and server tick stepping as later milestones.

The frozen M29 semantic SHA-256 is
`aec53757fe91829f4e425428a590b703595088ed02955b01ba41179ed4969b0b`.

## 1.16.0 - M28 Remote Chunk Observation

Status: GO for bounded native chunk-envelope observation.

- Added immutable `RemoteChunkObservation` and a chunk-capable multiplayer
  session extension.
- Parsed official `Packet51MapChunk` origin/dimensions and consumed its bounded
  compressed payload inside the b1.7.3 adapter.
- Repeated two fresh client/server scenarios and required complete
  `16 x 128 x 16` remote regions.
- Kept spawn-dependent origins and compressed sizes observational.
- Kept decompression, block/world construction, chunk caching, and server tick
  stepping as later milestones.

The frozen M28 observation SHA-256 is
`45179dd32117513e55cbf0698ec09e51440b3e3007188c100bcdd234257f0be4`.

## 1.15.0 - M27 Two-Client Multiplayer Chat

Status: GO for native peer chat through a bounded inbound packet pump.

- Added `ChatMultiplayerSession` for bounded chat send and receive.
- Added a fail-closed protocol-14 inbound codec for qualified login/play packet
  lengths, including chunk, entity, metadata, inventory, and block traffic.
- Connected two clients simultaneously to each official server and required
  exact two-player presence.
- Sent `worldline-m27` from `WorldlineA` and received the exact native broadcast
  on `WorldlineB` in two fresh scenarios.
- Kept chunk/world decoding, asynchronous pumping, remote-player rendering, and
  server tick stepping as later milestones.

The frozen M27 chat SHA-256 is
`7d264e3b365a4ab223d45cd95eb17aa90683ef123af51775defc120d7635aa12`.

## 1.14.0 - M26 Native Multiplayer Render Bridge

Status: GO for native offscreen rendering composed with a real multiplayer session.

- Combined official-server protocol-14 login/pose synchronization with mapped
  Minecraft `Tessellator` in one client process.
- Rendered connected state through native LWJGL and an OpenGL Pbuffer while
  requiring that no onscreen `Display` exists.
- Repeated two fresh client/server scenarios with exact pixel coverage and the
  M10-qualified mapped/official frame hash.
- Kept the complete Minecraft gameplay loop, chunk rendering, interactive GUI,
  and server tick stepping as explicit later milestones.

The frozen M26 bridge SHA-256 is
`c2d85227a2cb542e0c9b21aa77dd71a0bbfaab7162a1db6c0fb0955876dbb2ce`.

## 1.13.0 - M25 Multiplayer Player Movement

Status: GO for bounded movement accepted by the official server.

- Added relative movement intent to `PlayableMultiplayerSession`.
- Preserved the exact server-provided stance height when encoding native
  protocol-14 position/look packets.
- Used a within-spawn-block `+0.125 X` displacement independent of adjacent
  random terrain.
- Repeated two fresh official server scenarios and required exact target X/Y/Z
  in persisted player NBT.
- Kept arbitrary collision correction, continuous packet pumping, graphical
  client control, and server tick stepping as later milestones.

The frozen M25 movement SHA-256 is
`fb5715319d1347b180aea28652c173a9278d67dedbd3f6e9b486fe358d31f6d6`.

## 1.12.0 - M24 Multiplayer Play Pose

Status: GO for the bidirectional initial play-position exchange.

- Added immutable `PlayerPose` and a neutral playable multiplayer-session
  boundary for synchronization and look intent.
- Added a bounded protocol-14 play codec for spawn/time/chunk prelude packets,
  position decoding, native feet/stance acknowledgement, and client look.
- Extended persisted player observation with yaw and pitch from official NBT.
- Repeated two fresh official server scenarios and matched acknowledged
  position plus the exact requested `135.0/-22.5` rotation.
- Kept collision-qualified movement, the graphical client, continuous packet
  pumping, and server tick stepping as later milestones.

The frozen M24 play-pose SHA-256 is
`e43923f84231be276ae24a78a94f1d50aef3d5229dc59f10bcc5fd83c7cbc0db`.

## 1.11.0 - M23 Multiplayer Player Persistence

Status: GO for persisted multiplayer player observation.

- Added immutable `ServerPlayerState` and a persistent multiplayer server
  boundary for username, dimension, position, health, and inventory count.
- Added an original safe gzip/NBT reader for official server player files.
- Repeated two login/logout/save scenarios and verified bounded player state
  without freezing machine/world-dependent spawn coordinates.
- Kept movement, full play packets, graphical client, and tick determinism as
  later milestones.

The frozen M23 persistence SHA-256 is
`cce8512d97119d2c7fd010110a1760bebe7d86bed4f3d8cc1fefe39e58fb8928`.

## 1.10.0 - M22 Multiplayer Wire Harness

Status: GO for localhost protocol-14 login and player-presence control.

- Added neutral `MultiplayerSession`, `MultiplayerState`, and
  `MultiplayerServerRuntime` contracts.
- Added an original minimal b1.7.3 protocol-14 client for the native offline
  handshake and login response.
- Repeated two fresh scenarios where the official server lists exactly one
  Worldline client, then returns to an empty list after socket disconnect.
- Kept the official graphical client, movement, full play packets, and tick
  determinism as explicit later milestones.

The frozen M22 multiplayer SHA-256 is
`723f96819bd972ec5f2a4d932251840099f2d6472edf590c4386641a7d7e08f9`.

## 1.9.0 - M21 Dedicated Server Control

Status: GO for neutral command, save, and persisted-state control.

- Added `DedicatedServerRuntime`, `ServerLifecycle`, and immutable `ServerState`
  as the first game-neutral dedicated-server surface.
- Added a b1.7.3 process adapter that uses native console commands and reads
  persisted `level.dat` NBT without patching or decompiling the server.
- Booted two fresh official servers, set time to 6000, forced saves, observed
  the persisted time, and required clean native shutdown.
- Kept tick stepping, client login, packets, and multiplayer determinism as
  explicit later milestones.

The frozen M21 control SHA-256 is
`87035c21599513c04b6fe5b5622232a485a7f5c5e52778ecf11428ef671b4d4f`.

## 1.8.0 - M20 Official Server Bootstrap

Status: GO for official dedicated-server identity and lifecycle control.

- Added a frozen public descriptor for the unmodified Beta 1.7.3 dedicated
  server while keeping the proprietary JAR under ignored `local/artifacts/`.
- Added an HTTPS artifact acquisition tool that validates byte length, SHA-1,
  and SHA-256 before installing either the client or server JAR.
- Started two fresh localhost-only official servers, reached native readiness,
  issued `stop`, observed save/shutdown, and required clean exits.
- Kept server instrumentation, client connection, and multiplayer determinism
  as explicit later milestones.

The frozen M20 lifecycle SHA-256 is
`7d1edb19b978300465878cfade247ec0db7db37b9a5fbcfd9a595566bfb06b60`.

## 1.7.0 - M19 Forced Autosave

Status: GO for the default-off one-chunk save cap; the historical random spike
remains a non-claim.

- Added a look/jump/spin tower path and marked 60 loaded chunks dirty before
  native 40-tick autosaves.
- Compared vanilla's 24-chunk non-forced batch, an opt-in one-chunk cap, and
  the existing save-cancelled control on restored copies of one dense save.
- Proved that the cap keeps saves active while reducing the observed worst
  save; exact timings remain machine-local observations.
- Kept the cap default-off, the adaptive scheduler lab-only, and the M16 visual
  threshold unchanged.

The frozen M19 invariant-report SHA-256 is
`9ca8c14f03615b25891a8468a946bbbe7b889d8de747a8d0e03cb73665970bb1`.

## 1.6.0 - M18 Save Attribution

Status: GO for paired save-path attribution; the historical random spike
remains a non-claim.

- Parameterized the Aero capture skip-saves flag so M12-M17 keep cancelling
  non-forced saves while M18 can turn them back on.
- Injected one non-forced world save at a known tick from the Worldline
  capture mixin, without editing the pinned Aero checkout.
- Proved the skipped dense twin cancels that save and the live twin records
  `worldSaveMs` on the same line as compile, GC, heap, and allocation
  counters.
- Left the adaptive scheduler lab-only NO-GO and kept M16's framebuffer
  threshold unchanged.

The frozen M18 invariant-report SHA-256 is
`855ae55bc5944ae98d3fb6b66fe6840fc7561d425ce620b9ba45a55720f6c7bd`.

## 1.5.0 - M17 Scheduler Hardening

Status: GO for the three-scenario qualification matrix; NO-GO for scheduler
promotion.

- Added stationary-empty, stationary-dense, and moving-dense comparisons of
  vanilla retries, Aero's old governor, and the visible-first adaptive policy.
- Proved one adaptive completion per frame and eventual global drainage without
  background starvation across the matrix.
- Confirmed that the old governor retains backlog, while the adaptive envelope
  remains non-preemptive and can overshoot on one expensive rebuild.
- Kept moving-window readiness and scenario-dependent timing observational
  after qualifying repetitions changed their comparative direction.
- Corrected M13's historical scene-pressure gate to require exercised chunk
  compilation without freezing a machine-dependent 10 ms timing crossing.
- Stabilized checkpoint entities, daylight, weather, camera, and interpolation;
  all three framebuffer pairs still exceed M16's strict pixel tolerance.
- Packaged a default-off evaluation profile marked `lab-only-no-go`; the pinned
  Aero checkout remains unchanged.

The frozen M17 invariant-report SHA-256 is
`fa008e18e53b8d63003196e91d2b554f4ce973e602a68df4c7a7dc77096f7456`.

## 1.4.0 - M16 Adaptive Chunks

Status: GO for the visible-first adaptive scheduler; corrected startup rendering
does not reproduce the original fixed-state pixel parity.

- Added visible-debt bands of 2/4/6/8 accepted rebuilds under a 12 ms rebuild
  envelope while preserving one explicit accepted/deferred call per frame.
- Closed the first-300-frame visible readiness gap relative to vanilla and
  reduced the release-gate run's worst frame from 735.2 ms to 218.6 ms.
- Added a frozen-tick framebuffer oracle that fixes camera/interpolation, drains
  global chunk work, and compares every baseline/candidate RGBA pixel against a
  64-pixel, 2-channel-level decision threshold. M17's overlay correction now
  records a threshold violation.
- Added canonical save snapshot/restore so independently ordered world
  generation cannot contaminate the scheduler differential.
- Hardened the legacy M15 gate to compare normalized visible readiness and
  queue drainage instead of incomparable absolute frustum counts, and to apply
  its geometry threshold to the fixed-camera comparable cohort.
- Preserved the pinned Aero revision and the repository's per-file-only source
  limits.

The frozen M16 invariant-report SHA-256 is
`f274b0970e16939ba56b8f8796360d54c5f7981168a1e52e9d85da95585eb26b`.

## 1.3.0 - M15 Explicit Chunk Contract

Status: GO for the explicit accepted/deferred boundary and readiness evidence;
the fixed two-rebuild policy remains experimental.

- Added adapter-owned `COMPLETE`, `ACCEPTED_DEFERRED`, and `STALLED_DEFERRED`
  outcomes, mapped to vanilla's Boolean only at the render caller.
- Proved one contract invocation per frame with two real accepted rebuilds,
  next-frame resumption, and no same-frame retries or stalled batches.
- Measured dirty age and visible built/clean state from the first world frame;
  comparative readiness is reported but no longer frozen across machines.
- Added an exact Tessellator vertex-stream oracle. Most common non-empty chunks
  match exactly while nonzero tick-dependent temporal differences are retained.
- Retained only per-file source limits and left the pinned Aero checkout clean.

The frozen M15 invariant-report SHA-256 is
`64f635a1ed85ce0d9d30b468937b7803a06418e783f6ae8643da69877d597ba1`.

## 1.2.0 - M14 Chunk Backlog

Status: GO for caller semantics, initial-backlog isolation, and the bounded
non-retry prototype; the policy remains experimental.

- Proved that the primary render caller passes `forced=false` and retries
  `compileChunks` while it returns `false`, until the frame deadline.
- Measured thousands of dirty builders after warmup and continuous rebuilds in
  mostly quiet frames, attributing stable-camera pressure primarily to the
  initial queue rather than continuous new dirtiness.
- Added a smoke-only policy that uses vanilla priority ordering, performs two
  real rebuilds in one call per frame, and returns `true` without a retry storm.
- Added strict chunk-probe parsing, same-input fresh-world comparison, and
  explicit terrain-latency and visual-equivalence non-claims.

The frozen M14 invariant-report SHA-256 is
`65f43a875d18e96066441cb308fed7089bab8414b087f4398c1555211f2bae6a`.

## 1.1.0 - M13 Aero Differential

Status: GO for persistence isolation and the bounded chunk-path differential.

- Distinguished global BlockEntities from real entity blocks and proved that
  the 576 real fixture blocks persist while excess phantom entries disappear.
- Added equal-control dense and Aero-disabled captures; both exercise the same
  substantial chunk-compilation path and exploratory runs spike in both, so a
  stable dense amplification is not claimed.
- Exercised the optional compile governor on the render path and rejected it after
  the always-active control produced a hot retry storm.
- Added a strict Aero diagnostics adapter and a four-mode executable gate while
  retaining only per-file source limits.

The frozen M13 invariant-report SHA-256 is
`1759de8beeeef257a4027fd79f590ec7a72d364729863d1cb5fe373741399e80`.

## 1.0.0 - M12 Aero Reproduction

Status: GO for controlled real-scene capture and bounded spike reproduction.

- Added a test-only mapped runtime hook that creates a fixed-seed Aero MEGA
  world, forces the target chunks, fixes camera and velocity, and stops after a
  bounded 240-tick measurement window without modifying the Aero checkout.
- Captured two real Fabric Loader/StationAPI/LWJGL frame logs with dense Aero
  work and reproduced spikes localized to the chunk-compilation stage.
- Reused the M9 minimizer to reduce each stable-scene record window to one
  qualifying frame while retaining M11's neutral attribution boundary.
- Captured and hashed the generated save; M13 later distinguished its
  persistent real entity blocks from non-persistent phantom global entries.
- Removed repository-wide line caps while retaining enforced per-file limits:
  250 product lines, 300 harness lines, and 150 smoke/adapter lines.

M13 corrected the M12 oracle so stable single-call slow compiles are not
misreported as expanded logical-work counts. The corrected M12 invariant SHA-256 is
`804915ae89a1adef9f350adc020ed8a77986b2d3d4c1d84205009a4382ed051c`.

## 0.9.0 - M11 Aero Attribution

Status: GO for bounded work attribution and exact-candidate qualification.

- Added adapter-neutral frame-work comparison with explicit `LOGICAL_WORK`,
  `RUNTIME_STALL`, `MIXED`, and `INCONCLUSIVE` outcomes.
- Counted renderer work above the M10 Pbuffer without changing its frozen RGBA
  result and added an isolated Aero frame-log adapter.
- Pinned Aero Model Lib 3.0.0 at commit `436d65b`, ran all 222 core tests,
  built its StationAPI JAR and consumer, and loaded both test entrypoints in a
  real Fabric Loader/StationAPI client boot.
- Recorded the non-fatal startup diagnostic where showcase-block UV resolution
  occurs before atlas readiness; no historical-spike root cause is claimed.

The frozen M11 attribution SHA-256 is
`42e656576b70c53919761570abf016f93f76ddfbe49f3e40b79f2de0518eaecc`.

## 0.8.0 - M10 Native/Offscreen Render

Status: GO for the bounded render contract.

- Added a real 64 by 64 LWJGL/OpenGL Pbuffer lane isolated from the existing
  headless substitutions.
- Drew deterministic geometry through Minecraft's own `Tessellator`, verified
  exact pixel colors and coverage, and hashed the complete RGBA framebuffer.
- Added two mapped and two official-JAR processes with repetition, provenance,
  cross-boundary equality, and frozen-output checks.
- Investigated the original Aero target and recorded `artifact-absent` plus
  runtime compatibility `NOT_RUN`; no compatibility result was inferred.

The frozen M10 framebuffer SHA-256 is
`3f7da2d7ed9eeeff4c1ac7ad3767c82a5cb95b066cdb28bd3788e0cbcd3141ff`.

## Unreleased - Optimization Metadata SDK

- Added dependency-free, source-retained `OptimizationRef` metadata in its own
  optional module; it injects no runtime behavior or bytecode.
- Added a properties-backed record schema for status, defaults, behavior delta,
  risks, rollback, source symbols, and evidence.
- Added fail-closed canonical checks for unknown IDs, incomplete records,
  unevidenced decisions, unsafe defaults, and annotation/symbol tracking drift.
- Made repository ownership explicit: Worldline contains only the neutral SDK
  and its own records; mods own implementation-specific catalogs and Worldline
  evidence refers to their stable IDs.
- Added isolated positive and negative checker fixtures without coupling mod
  sources or implementation knowledge to Worldline.

## Unreleased - M2 Controlled Runtime

Status: GO.

- Promoted virtual clock, programmable input, RNG reseed, filesystem
  journal/failure injection, offline network, tick scheduler, and timer-thread
  supervision from experimental evidence to a stable milestone.
- The public product version remains 0.7.0 / M9. M2 does not add
  `worldline-api` types; boundary controls stay on the b1.7.3 adapter.
- Frozen evidence is the existing four-process 16-tick client state signature
  `e8cdeba39a44b772a70c48c0acd9ae3983f3d95a8c10c545df5d66fb953db554`.

## Unreleased - Semantic Mappings

Status: GO.

- Added `SemanticMapping` so a b1.7.3 symbol can carry role, category,
  reads/writes, dependencies, evidence, an optional official client alias,
  and confidence.
- Added `worldline-semantics` with a fail-closed catalog of the 24 control
  categories and 196 required roles, including both `symbols.map` files,
  adapter/oracle fields, item/recipe/domain surfaces, and the native
  autosave, chunk-save, and compile-chunk symbols.
- Added `AdapterManifest` so Worldline adapters declare catalog sites without
  placing Aero or other external types in `SemanticCatalog.standard()`.
- Added a fail-closed coverage gate so every named `symbols.map` symbol has
  a catalog role, plus `SemanticGraph` over static read/write/dep tokens.
- Added CLI `semantics show|graph|category|role` inspection without loading
  Minecraft.
- Trace CLI diffs print a catalog `role=` alias for known fields. Scenario
  minimization tries disposable lab/noise steps first. Frozen M6/M8 CLI-report
  hashes now include those role lines; M9 evidence uses 21 evaluator calls.
- Diverged conservation fields also print `invariant=<rule>` after the M6
  document. `block65` names `block-conservation`. Frozen M8/M9 hashes include
  that line.

## Unreleased - Invariant Engine

Status: GO.

- Added `ItemCensus` and `InvariantViolation` so observed item totals are
  immutable API values.
- Added `worldline-invariants` with `InvariantEngine` and `ItemConservation`.
  The first census is the baseline; later gains fail closed, losses do not.
- Added `GamePlayer.items()` and opt-in `watch` so each controlled tick samples
  player and world totals without opening a screen.
- World census includes dropped items and loaded container inventories.
- Item conservation is now consecutive and recipe-aware. A gain holds when a
  `RecipeBook` can account for it; unexplained creation still fails closed.
- Crafting container leftovers (empty buckets from milk) are folded into
  recipe outputs so cake no longer looks like item creation.
- `GameWorld.blocks()` and block-drop recipes explain harvest gains (stone
  to cobble, log to log), including sampled random quantities.
- `EntityCensus`, `CauseDrop`, and `DropBook` explain mob death, chicken
  eggs, and caught fish. Newly loaded chunk items are imports, not creation.
- Added `TimeMonotonic` and `InvariantEngine.standard` so world time cannot
  move backward.
- Added `EntitySpawn`, `BlockConservation`, `HealthConservation`, and
  `DurabilityConservation`. `standard(runtime)` also loads block transforms,
  fluid/fire/plant presence, food heal amounts, and host spawn rules.
- The controlled-client cycle watches `standard(runtime)` for 16 live ticks.
  Falling sand, thrown items, lit-block swaps, and `GameWorld.peaceful()`
  complete the world-tick cause book.
- Removed total line budgets for harness, smoke, and adapter. Only per-file
  ceilings remain.

## Unreleased - Game UI Tree

Status: GO.

- Added `UiMinecraftRuntime`, `GameUi`, and immutable `GameUiNode` values for a
  semantic inventory tree (`screen`, `slot`, `node`, `click`).
- Added a four-process official-JAR differential that opens, inspects, clicks,
  and closes the inventory screen without mapped types in the caller.
- Added `GameUiSpec` so Aero Machine Maker `guiComponents` and a live `GameUi`
  tree share role/name/index without pixels.
- Added a Flutter-inspired `Ui.screen/row/slot` declaration that flattens to
  the same spec. Layout widgets do not become DOM nodes.

The frozen GUI-tree SHA-256 is
`ab13a631ed766de32f2947fae1a6e0a86d9b6cde3cbc7e1557ff76f76ccc60cf`.

## 0.7.0 - M9 Automatic Scenario Minimization

Status: GO.

- Added canonical checksum-protected `.wlscenario` artifacts with bounded,
  ordered, adapter-neutral steps.
- Added exact first-divergence fingerprints and a deterministic delta debugger
  with cached evaluations, explicit budgets, and final one-step verification.
- Added CLI scenario creation/inspection with create-new output semantics.
- Added repeated real-runtime minimization across the two M8 mod versions,
  reducing nine noisy steps to a proven one-minimal three-step reproducer.

The frozen M9 minimization-report SHA-256 is
`706ff2a6fbeb2de5049749a573de95ba75ff43229326e7fd27a20aaf75b39a69`.

## 0.6.0 - M8 Differential Mod Testing

Status: GO.

- Added canonical `.wlmtest` results that bind mod identity, version, entrypoint,
  whole-JAR SHA-256, runtime/API declarations, and a canonical `v2` trace.
- Added stable mod-test comparison metadata and reused M6 first-divergence
  semantics without introducing a second trace comparison implementation.
- Extended the CLI and launcher with non-overwriting `mod test record` and
  equality/divergence-aware `mod test diff` commands.
- Added deterministic JAR packaging and repeated controlled-client evidence for
  a baseline and two versions of one mod, plus corrupt-result rejection.

The frozen M8 evidence-report SHA-256 is
`b08aa9f46b2d8522e6b8ac991553b2b6f946a63190d5956e59cbf6d544eb8938`.

## 0.5.0 - M7 General Mod Loading

Status: GO.

- Added a game-independent mod package module with strict canonical descriptor
  parsing, bounded JAR inspection, SHA-256 provenance, and explicit runtime/API
  compatibility results.
- Added descriptor-selected isolated entrypoint loading with type and code-origin
  checks; compatible code is never initialized before metadata acceptance.
- Extended the neutral CLI and repository launcher with `mod inspect` and stable
  compatible, incompatible, invalid-input, and usage exit codes.
- Added real controlled-client evidence for two independently selected mods and
  fail-closed runtime, API, entrypoint-type, malformed, and missing-descriptor cases.

The frozen M7 compatibility-report SHA-256 is
`bd13989879dba605a0cf790312c24a0f6947e87fb0b4d3ecd6f8cb265cbfb537`.

## 0.4.0 - M6 Trace Explorer

Status: GO.

- Added strict parsing and immutable models for schema-bearing canonical `v2`
  state traces.
- Added stable tabular rendering and structural first-divergence analysis for
  seed, schema, record labels/count, and individual field values.
- Extended the CLI and repository launcher with runtime-independent `trace
  show` and `trace diff` commands and explicit equality/divergence exit codes.
- Added fresh mapped/official trace equality, injected field divergence,
  reverse comparison, and malformed-schema rejection evidence.

The frozen M6 divergence-report SHA-256 is
`7eb4f707427c4e58ab3e481cc61f5801518325d5bbdfe045828325ab5ed2ea06`.

## 0.3.0 - M5 Reproduction Bundle

Status: GO.

- Added canonical `ReproductionBundle`, `ReplayProvider`, and `ReplayReport`
  contracts in an independently compiled module.
- Added a neutral replay CLI and a repository launcher that verifies local
  runtime inputs before starting the controlled b1.7.3 provider.
- Bundles embed the durable M4 snapshot while declaring the exact Worldline,
  runtime, official-client hash, and RetroMCP revision required for replay.
- Added two-process deterministic packing, copied-path CLI replay, official-JAR
  state comparison, and negative corruption/runtime/dependency evidence.

The frozen M5 bundle SHA-256 is
`840dca117939412dbba24594a1091c44d4b312b1e9700cec7aab7f47e0cc0181`.

## 0.2.0 - M4 Durable Snapshot

Status: GO.

- Added neutral `SnapshotMinecraftRuntime` and immutable, bounded
  `RuntimeSnapshot` contracts.
- Promoted the b1.7.3 replay checkpoint to a versioned canonical UTF-8 format
  with a body checksum and a frozen full-document SHA-256.
- Added strict parsing for runtime/version identity, field order, event count,
  numeric ranges, relative logical world sources, UTF-8, checksum, and exact
  canonical round-trip.
- Added cross-process capture and restore evidence, direct official-client
  state comparison, and executable corruption rejection.

The frozen M4 snapshot SHA-256 is
`a6e6589f9fdac1e40170f7a3b7fca7fc06b643b20a86249a464f9b2ab5b53bd2`.

## 0.1.0 - M3 Domain API

Status: GO.

- Added the opt-in `AutomatedMinecraftRuntime` without changing the v0.0.1
  lifecycle contract.
- Added neutral immutable block and position values plus stable world, entity,
  and local-player interfaces.
- Added lifecycle-guarded b1.7.3 implementations for world time, block
  read/write, active-entity snapshots, player state, teleportation, and hotbar
  selection.
- Added machine-verified mappings for every M3 field and method.
- Added a four-process differential oracle compiled independently against the
  official client JAR.

The frozen M3 signature is
`d38186377edc68f8080e568ffaba6559c4b3980fcf2a5311aac1b6ec7ebcc13c`.

## 0.0.1 - Controlled Tick

Status: GO.

Stable milestone contract:

- freeze and hash the official Minecraft Beta 1.7.3 client artifact;
- pin and verify the RetroMCP toolchain;
- reconstruct and compile the mapped client locally;
- boot the real client object graph without a native window;
- load a deterministic in-memory world;
- advance exactly one externally requested `Minecraft.runTick()`;
- match an independent oracle compiled against the official client JAR in two
  fresh subject and two fresh oracle JVMs.

The frozen first-tick signature is
`ac13115a73408c85eb80b931dc3004b4fd66b26a5512e8d4fb036eebf70ae780`.

Release qualification includes two cold RetroMCP reconstructions. Decompiled
`World` source was not byte-stable, so v0.0.1 explicitly guarantees frozen
inputs and oracle-verified observable behavior, not byte-identical decompiler
output.

Experimental capabilities shipped alongside the milestone include a reusable
b1.7.3 adapter, 16-tick state traces, deterministic external boundaries,
replay-backed checkpoints, hypothesis branches, semantic inventory GUI
actions, and isolated mod-JAR loading. These do not enlarge the stable v0.0.1
contract.
