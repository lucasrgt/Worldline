# Worldline Roadmap

The roadmap distinguishes completed evidence from stable public contracts.
Passing an experiment does not silently promote its API.

| Stage | Objective | Current state |
| --- | --- | --- |
| v0.0.1 | Headless b1.7.3 boot, world load, one manual client tick, official oracle match | GO - stable milestone |
| M2 | Virtual clock, input, RNG, filesystem, network, scheduler, and thread control | GO - stable milestone |
| v0.1.0 / M3 | Stable world, player, entity, and block automation API | GO - stable milestone |
| v0.2.0 / M4 | Durable snapshot format and cross-process restore | GO - stable milestone |
| v0.3.0 / M5 | Portable reproduction bundles and replay CLI | GO - stable milestone |
| v0.4.0 / M6 | Trace viewer and first-divergence explorer | GO - stable milestone |
| v0.5.0 / M7 | General mod loading and compatibility contracts | GO - stable milestone |
| v0.6.0 / M8 | Differential mod/version testing | GO - stable milestone |
| v0.7.0 / M9 | Automatic scenario minimization | GO - stable milestone |
| v0.8.0 / M10 | Native/offscreen render E2E and Aero investigation | GO - render stable; Aero artifact absent |
| v0.9.0 / M11 | Render-work attribution and exact Aero qualification | GO - bounded attribution and StationAPI load |
| v1.0.0 / M12 | Real Aero scene capture, repeated spike classification, and evidence minimization | GO - chunk-compilation spike reproduced; persistence split deferred |
| v1.1.0 / M13 | Aero persistence isolation, dense/empty differential, and compile-budget test | GO - real BEs persist; empty retains compile pressure; budget rejected |
| v1.2.0 / M14 | Chunk caller semantics, initial-backlog attribution, and bounded non-retry policy | GO - caller/backlog isolated; policy experimental |
| v1.3.0 / M15 | Explicit accepted/deferred contract, visible readiness, and geometry oracle | GO - contract qualified; fixed batch experimental |
| v1.4.0 / M16 | Visible-first adaptive chunk envelope and fixed-state framebuffer oracle | GO - scheduler qualified; post-overlay visual divergence |
| v1.5.0 / M17 | Three-scenario scheduler hardening and upstream evaluation profile | GO - matrix complete; promotion NO-GO |
| v1.6.0 / M18 | Save/GC/compile attribution of the historical random spike | GO - timeline colocated; spike NON-CLAIM |
| v1.7.0 / M19 | Forced dirty-set autosave and opt-in one-chunk save cap | GO - synthetic hitch bounded; cap default-off |
| v1.8.0 / M20 | Official b1.7.3 dedicated-server identity and lifecycle bootstrap | GO - two clean localhost boot/save/stop processes |
| v1.9.0 / M21 | Neutral dedicated-server command, save, and persisted-state control | GO - two official controller/server pairs |
| v1.10.0 / M22 | Protocol-14 localhost login, player presence, and clean disconnect | GO - two fresh wire-client/server scenarios |
| v1.11.0 / M23 | Persisted multiplayer player identity, position, health, and inventory | GO - two official player NBT observations |
| v1.12.0 / M24 | Bidirectional initial position exchange and persisted look action | GO - two official play-pose observations |
| v1.13.0 / M25 | Bounded deliberate movement accepted and persisted by the official server | GO - two exact target observations |
| v1.14.0 / M26 | Protocol-14 state composed with native offscreen Minecraft rendering | GO - two client/server render bridges |
| v1.15.0 / M27 | Two simultaneous clients and bounded inbound chat packet pump | GO - two exact peer broadcasts |
| v1.16.0 / M28 | Neutral bounded observation of official remote chunk envelopes | GO - two full chunk regions |
| v1.17.0 / M29 | Strict chunk inflation and coordinate-addressable neutral block view | GO - mapped layout oracle plus two official chunks |
| v1.18.0 / M30 | Packet50-qualified bounded multi-chunk remote-world cache | GO - load/unload oracle plus two official views |
| v1.19.0 / M31 | Server-authoritative Packet52/53 incremental block updates | GO - mapped fixture plus two official digs |
| v1.20.0 / M32 | Sustained protocol-14 cache composed with mapped native terrain geometry | GO - byte oracle plus two live frame deltas |
| v1.21.0 / M33 | Deliberate chunk-boundary traversal and rendered cache lifecycle | GO - strict/implicit oracle plus two official turnovers |
| v1.22.0 / M34 | Server-authoritative position correction and cache coherence | GO - byte oracle plus two official correction round trips |
| v1.23.0 / M35 | Bounded accepted-versus-corrected movement outcome | GO - persisted small moves plus collision rollbacks |
| v1.24.0 / M36 | Relative route continuation after authoritative correction | GO - three ordered outcomes plus persisted recovery |
| v1.25.0 / M37 | Explicit stop-on-correction route policy | GO - two outcomes, zero retry, persisted stop |
| v1.26.0 / M38 | Caller-supplied single movement fallback | GO - conditional fallback, zero primary retry, persisted result |
| v1.27.0 / M39 | Synchronous indexed route outcome observation | GO - caller-thread events identity-bound to final results |
| v1.28.0 / M40 | Observer-directed synchronous route cancellation | GO - explicit stop after immutable event; later movement absent |
| v1.29.0 / M41 | Immutable stopped-versus-exhausted route summary | GO - exact terminal event and identity-bound final outcome |
| v1.30.0 / M42 | Caller-owned opaque route correlation | GO - identity preserved in events and terminal summary; no registry |
| v1.31.0 / M43 | Bounded sequential correlated route batch | GO - per-route termination plus stop before unsent plan |
| v1.32.0 / M44 | Synchronous stable-index batch observation | GO - route indexes preserve embedded event indexes and identity |
| v1.33.0 / M45 | Batch-wide stop at a movement event boundary | GO - later alternatives and plans remain unsent |
| v1.34.0 / M46 | Exact terminal event for every batch return | GO - EVENT, AFTER_ROUTE, and EXHAUSTED identity-bound summaries |
| v1.35.0 / M47 | Immutable batch route/outcome/correction counts | GO - bounded aggregates without event replay or flattening |
| v1.36.0 / M48 | Server-authoritative inventory window and slot observation | GO - immutable Packet104 snapshot plus matching Packet103 deltas |
| v1.37.0 / M49 | Held hotbar selection with independent peer observation | GO - Packet16 selection proven through named Packet5 equipment update |
| v1.38.0 / M50 | Drop-current-item with local, peer, and persistence evidence | GO - Packet14 status 4 proven through Packet103 and Packet5 empty transitions |
| v1.39.0 / M51 | Immutable dropped-item spawn observation | GO - exact Packet21 stack with bounded position and velocity evidence |
| v1.40.0 / M52 | Named dropped-item collection lifecycle | GO - Packet21 entity correlated through Packet22 collector and Packet29 removal |
| v1.41.0 / M53 | Selected held-block placement with two remote caches | GO - Packet15 derived stack proven through Packet53, Packet103, and Packet5 |
| v1.42.0 / M54 | Single-chest open and immutable combined-window read | GO - Packet100 readUTF descriptor paired with exact 63-slot Packet104 view |
| v1.43.0 / M55 | Accepted personal-window left-click transactions | GO - exact Packet102 predictions committed only on correlated Packet106 true |
| v1.44.0 / M56 | Rejected personal transaction recovery | GO - immediate re-enable ACK plus atomic Packet104/cursor Packet103 resync |
| v1.45.0 / M57 | Personal 2x2 log-to-planks crafting | GO - four accepted predictions plus authoritative recovery audit |
| v1.46.0 / M58 | Explicit remote-window lifecycle | GO - Packet101 close confirmed by accepted window-0 no-op proof |
| v1.47.0 / M59 | Player-to-chest transfer and restart persistence | GO - accepted actions reconcile both views and survive reopen |
| v1.48.0 / M60 | Typed furnace loading and complete live smelt observation | GO - Packet103 slots and Packet105 progress produce exact glass output |
| v1.49.0 / M61 | Furnace glass output retrieval and restart persistence | GO - actions5/6 plus Packet200 move exact glass into persisted personal storage |
| v1.50.0 / M62 | Typed workbench open/read and asymmetric combined layout | GO - Packet100 declared9 maps to result+matrix+player Packet104 total46 |
| v1.51.0 / M63 | Three-wide workbench request/model preparation | GO - byte-exact right clicks drive an ACK-correlated row/cursor model |
| v1.52.0 / M64 | Exact workbench slabs output and persistence | GO - actions5/6 confirm result, consume grid, store and survive restart |
| v1.53.0 / M65 | Exact leather armor equipment and peer state | GO - actions1..8 map window slots5..8 to Packet5 slots4..1 and survive restart |
| v1.54.0 / M66 | Bounded armored player combat and local health | GO - Packet7 correlates dual Packet38 evidence and victim Packet8 20 to 18 |
| v1.55.0 / M67 | Exact single-chest retrieval and final persistence | GO - accepted actions move chest0 to personal36 and survive restart |
| v1.56.0 / M68 | Real StationAPI/Aero client login and remote render composition | GO - Packet1/13/51 readiness precedes bounded frames and Aero logs |
| v1.57.0 / M69 | Exact held-item swing request and named Packet18 peer observation | GO - production bytes and Packet20 identity qualify animation 1 without attack claims |
| v1.58.0 / M70 | Real Aero combat-event frame/log window | GO - observer applies Packet18 before Packet38, then completes bounded frames and Aero rows |
| v1.59.0 / M71 | Balanced paired Aero control/event acquisition | GO - exact common anchor plus descriptive selected-row summaries and pair deltas |
| v1.60.0 / M72 | Server-authored StationAPI content rendered by real Aero client | GO - exact identifier/coordinates/nonce message plus renderer return and bounded frames |
| v1.61.0 / M73 | Balanced absent/present synchronized Aero-content window | GO - tracked activation, 0 vs 16 structural treatment, descriptive selected-row deltas |
| v1.62.0 / M74 | Complete bounded renderer-interval census over the paired Aero fixture | GO - primitive in-memory capture and post-seal binary artifacts |
| v1.63.0 / M75 | Mirrored 0/1/4/16 Aero-call exposure ladder over a constant scene | GO - exact per-record treatment and descriptive whole-census summaries |
| v1.64.0 / M76 | Renderer registration/body/Aero-call decomposition over a constant sixteen-entity scene | GO - exact 0/0, 16/0, and 16/16 records; mixed descriptive timing |
| v1.65.0 / M77 | Direct renderer/fallback/empty-flush timing aligned to the complete census | GO - exact 16/16/2 call spans in two fresh same-plan replicas |
| v1.66.0 / M78 | Real warmed Aero cell-page enqueue/flush timing | GO - exact 16 enqueues, four cached pages/calls, zero fallback/rebuild |
| v1.67.0 / M79 | Single explicitly armed cold page rebuild | GO - cache 4-to-0-to-4, four deleted/compiled/rebuild deltas, zero fallback |
| v1.68.0 / M80 | Server-authored natural membership rebuild | GO - exact cell removal, membership 16-to-15, one rebuild, zero fallback |
| v1.69.0 / M81 | Server-authored natural two-page rebuild wave | GO - exact two-cell removal, membership 16-to-14, two rebuilds, zero fallback |
| v1.70.0 / M82 | Natural membership-wave cardinality ladder | GO - fixed-plan 1/2/4 targets, membership 15/14/12, rebuilds 1/2/2 |
| v1.71.0 / M83 | Constant-cardinality page-topology contrast | GO - two targets in one vs two pages, membership 14, rebuilds 1 vs 2 |
| v1.72.0 / M84 | Four-page-key constant-cardinality topology contrast | GO - three targets in one vs three cached pages, membership 13, rebuilds 1 vs 3 |
| v1.73.0 / M85 | Natural membership remove/restore recovery | GO - exact cell 16-to-15-to-16, direct fallback 0-to-1-to-0, one restore rebuild |
| v1.74.0 / M86 | Repeated same-cell membership recovery | GO - two generation-bound 16-to-15-to-16 cycles with exact fallback/rebuild recovery |
| v1.75.0 / M87 | Sequential two-cell membership recovery | GO - index0 fallback recovery followed by index1 immediate-rebuild recovery |
| v1.76.0 / M88 | Reverse two-cell membership recovery | GO - index1 rebuild-first followed by index0 fallback-second under the same plan |
| v1.77.0 / M89 | Sibling-cell membership recovery | GO - index4 matches index0's two-member-page fallback and restore rebuild |
| v1.78.0 / M90 | Larger-page sibling recovery | GO - index2 matches index1's six-member-page batched rebuild route |
| v1.79.0 / M91 | Larger-page depletion recovery | GO - indices1/2 deplete six-member page to four and reverse-recover with fully batched rebuilds |
| v1.80.0 / M92 | Third-member depletion recovery | GO - indices1/2/3 deplete six-member page to three and reverse-recover with fully batched rebuilds |
| v1.81.0 / M93 | Full-page depletion recovery | GO - exact six-member page crosses batched, direct-one, empty, and reverse-recovery states |
| v1.82.0 / M94 | Default-TTL page recovery | GO - empty target cache expires 4-to-3 under default600, then direct-one/rebuild-two restores cache4 |
| v1.83.0 / M95 | Four-page capacity-three cache thrash | GO - every retained record keeps cache3 and rebuilds/evicts exactly two pages |
| v1.84.0 / M96 | Four-page capacity-two bounded thrash | GO - rebuild mode 3 or 4 with exact per-record eviction coupling |
| v1.85.0 / M97 | Four-page capacity-one thrash | GO - every retained record rebuilds and evicts all four pages |
| v1.86.0 / M98 | Configured-zero protected cache floor | GO - literal max0 retains one protected page and rebuilds/evicts four per record |
| v1.87.0 / M99 | Rebuild-budget fallback under one-entry cache pressure | GO - two pages rebuild and four remaining instances render directly per record |
| v1.88.0 / M100 | Rebuild-budget-one alternating fallback | GO - exact pageCalls2/direct4 and pageCalls1/direct10 alternation |
| v1.89.0 / M101 | Rebuild-budget-zero direct path | GO - cache stays empty and all sixteen instances render directly |
| v1.90.0 / M102 | Negative-one unlimited-rebuild sentinel | GO - all four pages rebuild with zero direct fallback |
| v1.91.0 / M103 | Explicit pages-disabled immediate direct control | GO - sixteen direct overload calls despite reset public counters |
| v1.92.0 / M104 | Balanced paired pages control | GO - same-plan enabled/disabled structural paths under one recorder schema |
| v1.93.0 / M105 | Balanced paired cache-capacity control | GO - capacity1 thrash versus negative-one stable four-page retention |
| v1.94.0 / M106 | Balanced paired minimum-instance control | GO - minimum2 four-page replay versus minimum5 mixed two-page/four-direct path |
| v1.95.0 / M107 | Balanced paired skip-individual control | GO - managed pre-dispatch queue versus manual renderer queue under the same four pages |
| v1.96.0 / M108 | Balanced paired cell-size control | GO - same aligned scene forms four size-two pages versus one size-eight page |
| v1.97.0 / M109 | Cell-size lower-bound clamp control | GO - raw zero and explicit one converge on sixteen single-member direct cells |
| v1.98.0 / M110 | Cell-size upper-bound clamp control | GO - raw thirty-three and explicit thirty-two converge on one cached page |
| v1.99.0 / M111 | Fixed-seed absolute terrain | GO - two fresh official worlds match the complete ID volume and top-Y/ID/metadata surface of chunk `(0,0)` |
| v1.100.0 / M112 | Fixed-seed light planes | GO - two fresh official worlds match all block-light and sky-light nibbles and histograms in chunk `(0,0)` |
| v1.101.0 / M113 | Causal vanilla lighting | GO - one official glowstone placement produces an exact 68-sample block-light transition after fresh chunk reload |
| v1.102.0 / M114 | Causal vanilla water flow | GO - one official dig opens a generated-water floor cell and settles exactly one block to water `9:8` |
| v1.103.0 / M115 | Causal lever activation | GO - empty-hand Packet15 toggles one stabilized official side lever from `69:1` to `69:9` with an exact one-state delta |
| v1.104.0 / M116 | Lever-to-wire propagation | GO - official dust placement yields wire `55:0`, then lever activation publishes exact power 15 with two-state delta |
| v1.105.0 / M117 | Lever-to-wire recovery | GO - a second official lever activation returns the exact powered `9/15` pair to `1/0` with two-state delta |
| v1.106.0 / M118 | Powered iron-door consumer | GO - one side lever opens both official block-71 halves with an exact three-state delta |
| v1.107.0 / M119 | Falling-sand gravity | GO - removing one official stone support settles sand down one cell with an exact two-state delta |
| v1.108.0 / M120 | Horizontal water propagation | GO - opening the sole exit of a bounded source-water trench produces exact target `9:1` |
| v1.109.0 / M121 | Fixed-seed 3x3 terrain region | GO - two fresh restarted worlds match aggregate solids, exact surfaces and internal solid seams across nine chunks |
| v1.110.0 / M122 | Fixed-seed 3x3 light planes | GO - both 294,912-sample vanilla light planes and every histogram bin repeat after clean restart |
| v1.111.0 / M123 | Cross-chunk causal lighting | GO - one edge glowstone propagates exact block-light deltas into the neighboring water-filled chunk |
| v1.112.0 / M124 | Cross-chunk light recovery | GO - removing the edge source reverses both deltas and restores both complete light planes |
| v1.113.0 / M125 | Cross-chunk water propagation | GO - a bounded source at x=15 changes exactly one target cell in chunk 1 to water `9:1` |
| v1.114.0 / M126 | Cross-chunk redstone propagation | GO - a lever in chunk 1 powers an attached wire in chunk 0 from `0` to `15` |
| v1.115.0 / M127 | Cross-chunk redstone recovery | GO - fresh-client deactivation restores lever and wire with one inverse delta per chunk and zero residual states |
| v1.116.0 / M128 | Cross-chunk iron-door consumer | GO - a lever in chunk 1 opens both exact door halves in chunk 0 with a `2 + 1` delta partition |
| v1.117.0 / M129 | Cross-chunk iron-door recovery | GO - fresh-client deactivation closes both door halves and restores both complete chunks exactly |
| v1.118.0 / M130 | Official Nether login | GO - opt-in Nether profile accepts a dimension-`-1` player, publishes exact terrain and persists the dimension |
| v1.119.0 / M131 | Dual-dimension protocol sessions | GO - simultaneous `0`/`-1` clients retain typed Packet1 state and Packet9 changes clear old chunks |
| v1.120.0 / M132 | Nether portal activation | GO - fourteen accepted obsidian placements plus flint and steel produce six persisted official portal blocks |
| v1.121.0 / M133 | Nether portal traversal | GO - active portal residence emits Packet9 `0→-1`, repopulates the cache with Nether terrain and creates the counterpart portal |
| v1.122.0 / M134 | Nether portal roundtrip | GO - generated-portal discovery, bounded cooldown and re-entry produce Packet9 `-1→0` and a valid Overworld portal view |
| v1.123.0 / M135 | Player death and respawn | GO - official void damage reaches a nonpositive Packet8 state, then a signed Packet9 request restores dimension 0, health 20 and a loaded spawn chunk |
| v1.124.0 / M136 | Nether death respawn | GO - a signed Packet9 `-1` request returns a dead Nether player to dimension 0 and replaces the old cache with lit Overworld chunks |
| v1.125.0 / M137 | TNT explosion | GO - an official fuse emits Beta-specific Packet60, removes a constructed support in the live cache and persists the blast |
| v1.126.0 / M138 | Horizontal lava | GO - opening one raised stone-trench cell schedules exact still-lava target `11:2` with one scoped persistent delta |
| v1.127.0 / M139 | Water-lava reaction | GO - still water `9:0` beside still lava `11:0` produces persisted obsidian `49:0` with exactly two scoped deltas |
| v1.128.0 / M140 | Bonemeal tree growth | GO - Packet15 bonemeal changes oak sapling root `6:0→17:0` and persists a bounded-positive trunk and canopy |
| v1.129.0 / M141 | Pig spawner observation | GO - two peers correlate one official default-spawner pig identity through strictly decoded Packet24 and metadata |
| v1.130.0 / M142 | Lever-powered piston extension | GO - one official piston event moves exact stone, creates head `34:4` and persists a four-cell raised delta |
| v1.131.0 / M143 | Normal-piston retraction | GO - fresh-session deactivation retracts base/head while exact displaced stone remains invariant |
| v1.132.0 / M144 | Sticky-piston pullback | GO - the matched sticky fixture retracts stone into the former head cell with four exact persisted deltas |
| v1.133.0 / M145 | Two-block piston chain | GO - distinct stone/cobblestone payloads shift one cell with five exact persisted raised deltas |
| v1.134.0 / M146 | Obsidian piston rejection | GO - a powered normal piston leaves exact obsidian and its destination invariant with only the lever changing |
| v1.135.0 / M147 | Piston push limit | GO - matched alternating chains prove twelve blocks move and thirteen blocks are rejected |
| v1.136.0 / M148 | Pig AI movement | GO - two peers apply the same official quantized horizontal movement to one shared pig identity |
| v1.137.0 / M149 | Pig death | GO - two peers observe Packet38 status 3 and Packet29 destroy after one official diamond-sword Packet7 |
| v1.138.0 / M150 | Pig pork drop | GO - adapter-owned identity reuses movement then death and shares Packet21 porkchop 319 |
| v1.139.0 / M151 | Netherrack fire | GO - flint and steel places fire 51 on netherrack and the flame survives hold plus restart |
| v1.140.0 / M152 | Fire wool consumption | GO - wool adjacent to netherrack fire is consumed while the netherrack flame remains |
| v1.141.0 / M153 | Dispenser eject | GO - Trap window load plus lever power ejects cobblestone Packet21 |
| v1.142.0 / M154 | Boat spawn | GO - Packet15 boat 333 in still water emits shared Packet23 type 1 |
| v1.143.0 / M155 | Minecart spawn | GO - Packet15 minecart 328 on rail 66 emits shared Packet23 type 10 |
| v1.144.0 / M156 | Farmland hydration | GO - wooden hoe tills dirt to 60 and adjacent water hydrates 60:7 |
| v1.145.0 / M157 | Bow arrow | GO - Packet15 bow 261 emits shared Packet23 type 60 with actor thrower |
| v1.146.0 / M158 | Bed | GO - daytime Packet3 refusal, night Packet17 occupy, SMP skip to morning |
| v1.147.0 / M159 | Sugar cane | GO - water-adjacent reed 83 grows to height 2+ and persists |
| v1.148.0 / M160 | Cake eat | GO - BlockCake 92:0 to 92:1 heals Packet8 17 to 20 |
| v1.149.0 / M161 | Snowball | GO - Packet15 snowball 332 emits shared Packet23 type 61 with thrower 0 |
| v1.150.0 / M162 | Wooden door | GO - item 324 places 64:0/64:8 and empty-hand toggles open/close |
| v1.151.0 / M163 | Trapdoor | GO - item 96 on east face toggles 96:3 to 96:7 and back |
| v1.152.0 / M164 | Pressure plate | GO - stone plate 70:0 powers to 70:1 under the actor and depowers |
| v1.153.0 / M165 | Stone button | GO - button 77:1 pulses to 77:9 and depowers after 20 ticks |
| v1.154.0 / M166 | Note block | GO - empty-hand click emits Packet54 instrument 1 pitch 1 |
| v1.155.0 / M167 | Cactus | GO - cactus 81 on sand 12 persists through hold and restart |
| v1.156.0 / M168 | Water bucket | GO - empty bucket 325 picks up still water 9 into 326 |
| v1.157.0 / M169 | Egg throw | GO - Packet15 egg 344 emits shared Packet23 type 62 with thrower 0 |
| v1.158.0 / M170 | Repeater | GO - repeater 93:3 powers to 94:3 from a lever and returns |
| v1.159.0 / M171 | Pumpkin | GO - pumpkin 86:1 from look yaw -90 persists |
| v1.160.0 / M172 | Wooden pressure plate | GO - wooden plate 72:0 powers to 72:1 under the actor and depowers |
| v1.161.0 / M173 | Fence | GO - two adjacent fences 85:0 persist |
| v1.162.0 / M174 | Ladder | GO - ladder 65:5 on an east face persists |
| v1.163.0 / M175 | Torch | GO - floor torch 50:5 persists |
| v1.164.0 / M176 | Sign | GO - standing sign 63 Packet130 text survives login |
| v1.165.0 / M177 | Painting | GO - Packet25 painting spawn is shared across two peers |
| v1.166.0 / M178 | Jukebox | GO - gold disc 2256 inserts into jukebox 84:1 via Packet61 1005 |
| v1.167.0 / M179 | Wheat | GO - seeds 295 plant wheat 59:0 on hydrated farmland 60:7 |
| v1.168.0 / M180 | Fishing rod | GO - rod 346 emits shared Packet23 type 90 with thrower 0 |
| v1.169.0 / M181 | Lava bucket | GO - empty bucket 325 picks up still lava 11 into 327 |
| v1.170.0 / M182 | Redstone torch | GO - floor redstone torch 76:5 persists |
| v1.171.0 / M183 | Rails | GO - rail 66:0 north-south with look yaw 0 persists |
| v1.172.0 / M184 | Powered rail | GO - unpowered powered rail 27:0 persists |
| v1.173.0 / M185 | Detector rail | GO - unpowered detector rail 28:0 persists |
| v1.174.0 / M186 | Oak stairs | GO - oak stairs 53:0 east with look yaw -90 persists |
| v1.175.0 / M187 | Cobble stairs | GO - cobble stairs 67:0 east with look yaw -90 persists |
| v1.176.0 / M188 | Stone slab | GO - single stone slab 44:0 persists |
| v1.177.0 / M189 | Bookshelf | GO - bookshelf 47:0 persists |
| v1.178.0 / M190 | Jack-o-lantern | GO - jack-o-lantern 91:1 with look yaw -90 persists |
| v1.179.0 / M191 | Glowstone | GO - glowstone 89:0 persists in the Overworld |
| v1.180.0 / M192 | Soul sand | GO - soul sand 88:0 persists in the Overworld |
| v1.181.0 / M193 | Ice | GO - ice 79:0 persists |
| v1.182.0 / M194 | Snow block | GO - snow block 80:0 persists |
| v1.183.0 / M195 | Cobweb | GO - cobweb 30:0 persists |
| v1.184.0 / M196 | Glass | GO - glass 20:0 persists |
| v1.185.0 / M197 | Wool | GO - white wool 35:0 persists |
| v1.186.0 / M198 | Dandelion | GO - yellow flower 37:0 on dirt 3 persists |
| v1.187.0 / M199 | Rose | GO - rose 38:0 on dirt 3 persists |
| v1.188.0 / M200 | Brown mushroom | GO - brown mushroom 39:0 in a dark pocket persists |
| v1.189.0 / M201 | Red mushroom | GO - red mushroom 40:0 in a dark pocket persists |
| v1.190.0 / M202 | Sapling | GO - oak sapling 6:0 on dirt 3 persists |
| v1.191.0 / M203 | Snow layer | GO - snow layer 78:0 persists |
| v1.192.0 / M204 | Clay | GO - clay 82:0 persists |
| v1.193.0 / M205 | Brick | GO - brick 45:0 persists |
| v1.194.0 / M206 | Sponge | GO - sponge 19:0 persists |
| v1.195.0 / M207 | Sandstone | GO - sandstone 24:0 persists |
| v1.196.0 / M208 | Oak log | GO - oak log 17:0 persists |
| v1.197.0 / M209 | Leaves | GO - oak leaves 18:8 persist beside log 17:0 |
| v1.198.0 / M210 | Oak planks | GO - oak planks 5:0 persist |
| v1.199.0 / M211 | Double slab | GO - double stone slab 43:0 persists |
| v1.200.0 / M212 | Gold block | GO - gold block 41:0 persists |
| v1.201.0 / M213 | Iron block | GO - iron block 42:0 persists |
| v1.202.0 / M214 | Diamond block | GO - diamond block 57:0 persists |
| v1.203.0 / M215 | Lapis block | GO - lapis block 22:0 persists |
| v1.204.0 / M216 | Obsidian | GO - obsidian 49:0 persists |
| v1.205.0 / M217 | Mossy cobble | GO - mossy cobble 48:0 persists |
| v1.206.0 / M218 | Gravel | GO - supported gravel 13:0 persists |
| v1.207.0 / M219 | TNT place | GO - unprimed TNT 46:0 persists |
| v1.208.0 / M220 | Workbench | GO - workbench 58:0 persists |
| v1.209.0 / M221 | Furnace | GO - idle furnace 61:2 persists |
| v1.210.0 / M222 | Cobble | GO - cobblestone 4:0 persists |
| v1.211.0 / M223 | Dirt | GO - dirt 3:0 persists |
| v1.212.0 / M224 | Netherrack | GO - Overworld netherrack 87:0 persists |
| v1.213.0 / M225 | Coal ore | GO - coal ore 16:0 persists |
| v1.214.0 / M226 | Iron ore | GO - iron ore 15:0 persists |
| v1.215.0 / M227 | Gold ore | GO - gold ore 14:0 persists |
| v1.216.0 / M228 | Diamond ore | GO - diamond ore 56:0 persists |
| v1.217.0 / M229 | Redstone ore | GO - unlit redstone ore 73:0 persists |
| v1.218.0 / M230 | Lapis ore | GO - lapis ore 21:0 persists |
| v1.219.0 / M231 | Dispenser place | GO - dispenser 23:3 from look yaw 180 persists |
| v1.220.0 / M232 | Chest place | GO - chest 54:0 persists |
| v1.221.0 / M233 | Note block place | GO - note block 25:0 persists |
| v1.222.0 / M234 | Sandstone slab | GO - sandstone slab 44:1 persists |
| v1.223.0 / M235 | Wood slab | GO - wood slab 44:2 persists |
| v1.224.0 / M236 | Cobble slab | GO - cobble slab 44:3 persists |
| v1.225.0 / M237 | Stone | GO - stone 1:0 persists |
| v1.226.0 / M238 | Grass | GO - grass 2:0 persists |
| v1.227.0 / M239 | Sand | GO - supported sand 12:0 persists |
| v1.228.0 / M240 | Bed place | GO - bed foot 26:0 and head 26:8 persist |
| v1.229.0 / M241 | Iron door place | GO - iron door 71:0 / 71:8 persist |
| v1.230.0 / M242 | Lever place | GO - east-face lever 69:1 persists |
| v1.231.0 / M243 | Redstone wire | GO - unpowered wire 55:0 persists |
| v1.232.0 / M244 | Cake place | GO - uneaten cake 92:0 persists |
| v1.233.0 / M245 | Wall sign | GO - wall sign 68:5 with Packet130 text persists |
| v1.234.0 / M246 | Spruce log | GO - spruce log 17:1 persists |
| v1.235.0 / M247 | Birch log | GO - birch log 17:2 persists |
| v1.236.0 / M248 | Orange wool | GO - orange wool 35:1 persists |
| v1.237.0 / M249 | Yellow wool | GO - yellow wool 35:4 persists |
| v1.238.0 / M250 | Red wool | GO - red wool 35:14 persists |
| v1.239.0 / M251 | Black wool | GO - black wool 35:15 persists |
| v1.240.0 / M252 | Blue wool | GO - blue wool 35:11 persists |
| v1.241.0 / M253 | Green wool | GO - green wool 35:13 persists |
| v1.242.0 / M254 | Water place | GO - water bucket 326 places still water 9:0 |
| v1.243.0 / M255 | Lava place | GO - lava bucket 327 places still lava 11:0 |
| v1.244.0 / M256 | Chest minecart | GO - item 342 on rail emits Packet23 type 11 |
| v1.245.0 / M257 | Furnace minecart | GO - item 343 on rail emits Packet23 type 12 |
| v1.246.0 / M258 | Bread eat | GO - bread 297 heals Packet8 15 to 20 |
| v1.247.0 / M259 | Cooked pork eat | GO - cooked pork 320 heals Packet8 12 to 20 |
| v1.248.0 / M260 | Apple eat | GO - apple 260 heals Packet8 16 to 20 |
| v1.249.0 / M261 | Golden apple eat | GO - golden apple 322 heals Packet8 10 to 20 |
| v1.250.0 / M262 | Cookie eat | GO - cookie 357 heals Packet8 19 to 20 |
| v1.251.0 / M263 | Stew eat | GO - stew 282 heals Packet8 12 to 20 and leaves bowl 281 |
| v1.252.0 / M264 | Raw pork eat | GO - raw pork 319 heals Packet8 17 to 20 |
| v1.253.0 / M265 | Fish eat | GO - raw fish 349 heals Packet8 18 to 20 |
| v1.254.0 / M266 | Cooked fish eat | GO - cooked fish 350 heals Packet8 15 to 20 |
| v1.255.0 / M267 | Milk bucket | GO - milk 335 becomes empty bucket 325 with no heal |
| v1.256.0 / M268 | Flint steel fire | GO - flint-and-steel 259 places fire 51:0 |
| v1.257.0 / M269 | Shears leaves | GO - shears 359 drop leaf item 18; bare hand does not |
| v1.258.0 / M270 | Iron helmet | GO - iron helmet 306 equips armor slot 5 / Packet5 4 |
| v1.259.0 / M271 | Gold chestplate | GO - gold chestplate 315 equips armor slot 6 / Packet5 3 |
| v1.260.0 / M272 | Diamond leggings | GO - diamond leggings 312 equips armor slot 7 / Packet5 2 |
| v1.261.0 / M273 | Chain boots | GO - chain boots 305 equips armor slot 8 / Packet5 1 |
| v1.262.0 / M274 | Falling gravel | GO - gravel 13:0 falls one cell after support removal |
| v1.263.0 / M275 | Cactus damage | GO - cactus contact drops Packet8 20 to 19 |
| v1.264.0 / M276 | Fire damage | GO - netherrack fire drops Packet8 20 to 19 |
| v1.265.0 / M277 | Wooden door open | GO - empty-hand Packet15 opens 64:0/64:8 to 64:4/64:12 |
| v1.266.0 / M278 | Trapdoor toggle | GO - empty-hand Packet15 opens trapdoor 96:3 to 96:7 |
| v1.267.0 / M279 | Button press | GO - stone button pulses 77:1 to 77:9 and back |
| v1.268.0 / M280 | Magenta wool | GO - magenta wool 35:2 persists |
| v1.269.0 / M281 | Light blue wool | GO - light-blue wool 35:3 persists |
| v1.270.0 / M282 | Lime wool | GO - lime wool 35:5 persists |
| v1.271.0 / M283 | Pink wool | GO - pink wool 35:6 persists |
| v1.272.0 / M284 | Gray wool | GO - gray wool 35:7 persists |
| v1.273.0 / M285 | Light gray wool | GO - light-gray wool 35:8 persists |
| v1.274.0 / M286 | Cyan wool | GO - cyan wool 35:9 persists |
| v1.275.0 / M287 | Purple wool | GO - purple wool 35:10 persists |
| v1.276.0 / M288 | Brown wool | GO - brown wool 35:12 persists |
| v1.277.0 / M289 | Spruce sapling | GO - spruce sapling 6:1 persists |
| v1.278.0 / M290 | Birch sapling | GO - birch sapling 6:2 persists |
| v1.279.0 / M291 | Spruce leaves | GO - spruce leaves 18:9 persist |
| v1.280.0 / M292 | Birch leaves | GO - birch leaves 18:10 persist |
| v1.281.0 / M293 | Sticky piston place | GO - sticky piston 29:1 persists |
| v1.282.0 / M294 | Piston place | GO - piston 33:1 persists |
| v1.283.0 / M295 | Pressure plates | GO - wood 72 and stone 70 power/unpower |
| v1.284.0 / M296 | Furnace smelts | GO - iron, gold, and pork recipes |
| v1.285.0 / M297 | Basic crafts | GO - planks, sticks, and torches |
| v1.286.0 / M298 | Wood tool crafts | GO - wooden sword/pick/axe/shovel/hoe |
| v1.287.0 / M299 | Stone tool crafts | GO - stone sword/shovel/pick/axe/hoe |
| v1.288.0 / M300 | Ore pick breaks | GO - cobble, coal, and diamond drops |
| v1.289.0 / M301 | Axe log breaks | GO - oak, spruce, and birch log drops |
| v1.290.0 / M302 | Shovel soft breaks | GO - dirt, sand, gravel, and clay drops |
| v1.291.0 / M303 | Crop harvests | GO - wheat, cane, and cactus drops |
| v1.292.0 / M304 | Farmland set | GO - hoe till 3→60 and trample 60→3 |
| v1.293.0 / M305 | Plant growth | GO - wheat bonemeal plus cactus and cane height |
| v1.294.0 / M306 | Closables | GO - wooden door and trapdoor close |
| v1.295.0 / M307 | Env damage | GO - drown, suffocate, and lava hurt |
| v1.296.0 / M308 | Fragile set | GO - ice break, glass break, ice melt |
| v1.297.0 / M309 | Rail power | GO - detector 28:8 and powered rail 27:8 |
| v1.298.0 / M310 | Vehicle rides | GO - boat type 1 and minecart type 10 attach |
| v1.299.0 / M311 | Storage carts | GO - chest-cart window and furnace-cart spawn |
| GUI tree | Neutral inventory Game UI tree with official-JAR match | GO - stable milestone |
| Invariant engine | Six fail-closed rules on live `watch(standard(runtime))` | GO - stable milestone |
| Semantic mappings | Closed 24-category catalog, adapter manifests, and static role graph | GO - stable milestone |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v1.299.0 direction

Worldline's active roadmap is vanilla-only until its Beta 1.7.3 model reaches
state of the art. M111 establishes deterministic block identity, M112 adds the
initial light planes, and M113 proves the first server-authored causal light
transition, and M114 qualifies the first scheduled water response. The active
sequence now deepens fluid behavior and advances from M115's component state
through M119's block gravity, M120's bounded horizontal fluid transition and
M121's nine-chunk fixed-seed surface, M122's exact regional light planes and
M123's first cross-chunk causal transition, M124's exact recovery, M125's
cross-chunk fluid flow, M126's cross-chunk redstone signal, M127's exact
recovery, M128's multiblock consumer, M129's exact consumer recovery and M130's
first official Nether session, M131's typed dual-dimension boundary, M132's
official portal activation, M133's live traversal, M134's complete roundtrip
M135's same-dimension death/respawn lifecycle and M136's Nether-to-Overworld
respawn, M137's first full explosion boundary, M138's scheduled lava and M139's
first fluid-material reaction and M140's first player-triggered vegetation
generation and M141's first living-entity identity into movement, entity AI,
additional physics/worldgen and dimensions
and persistence/multiplayer composition. M142 adds the first redstone-driven
block displacement, M143 proves non-sticky retention, and M144 proves the exact
sticky pullback contrast, M145 proves a fully observable two-block chain, and
M146 proves official rejection of one immovable obsidian payload, and M147
closes the exact twelve-block capacity versus thirteen-block rejection. The
M148 adds the first server-authored living-entity movement transition, M149
closes that identity through official diamond-sword death, and M150 requires
the adapter-owned movement-then-death path plus a shared raw porkchop drop.
M151 opens the official fire-material rule that netherrack keeps its flame,
and M152 proves adjacent wool is consumed while that flame remains. The next
milestones should deepen vehicles, projectiles, farming, beds, hostile AI or
another uncovered vanilla system rather than returning to the already bounded
piston matrix. M178 opens official jukebox disc insert through Packet61.
M179 plants wheat `59:0` on hydrated farmland. M180 casts a fishing hook as
Packet23 type `90`. M181 picks up still lava into bucket `327`. M182 places
floor redstone torch `76:5`. M183 places north-south rail `66:0`. M184 places unpowered powered rail
`27:0`. M185 places unpowered detector rail `28:0`. M186 places east oak stairs
`53:0`. M187 places east cobble stairs `67:0`. M188 places single stone slab `44:0`. M189 places bookshelf `47:0`. M190 places jack-o-lantern `91:1`. M191 places glowstone `89:0`. M192 places soul sand `88:0`. M193 places ice `79:0`. M194 places snow block `80:0`. M195 places cobweb `30:0`. M196 places glass `20:0`. M197 places white wool `35:0`. M198 plants yellow flower `37:0` on dirt. M199 plants rose `38:0` on dirt. M200 places brown mushroom `39:0` in a
dark pocket. M201 places red mushroom `40:0` in a dark pocket. M202 plants oak sapling
`6:0` on dirt. M203 places snow layer `78:0`. M204 places clay `82:0`. M205 places brick `45:0`. M206 places sponge `19:0`. M207 places sandstone `24:0`. M208 places oak log `17:0`. M209 places oak leaves `18:8` beside a log. M210 places oak planks `5:0`. M211 places double stone slab `43:0`. M212 places gold block `41:0`. M213 places iron block `42:0`. M214 places diamond block `57:0`. M215 places lapis block `22:0`. M216 places obsidian `49:0`. M217 places mossy cobble `48:0`. M218 places supported gravel `13:0`. M219 places unprimed TNT `46:0`. M220 places workbench `58:0`. M221 places idle furnace `61:2`. M222 places cobblestone `4:0`. M223 places dirt `3:0`. M224 places Overworld netherrack `87:0`. M225 places coal ore `16:0`. M226 places iron ore `15:0`. M227 places gold ore `14:0`. M228 places diamond ore `56:0`. M229 places unlit redstone ore `73:0`. M230 places lapis ore `21:0`. M231 places dispenser `23:3` from look yaw `180`. M232 places chest `54:0`. M233 places note block `25:0`. M234 places sandstone slab `44:1`. M235 places wood slab `44:2`. M236 places cobble slab `44:3`. M237 places stone `1:0`. M238 places grass `2:0`. M239 places supported sand `12:0`. M240 places bed foot `26:0` and head `26:8`. M241 places iron door `71:0` / `71:8`. M242 places east-face lever `69:1`. M243 places unpowered redstone wire `55:0`. M244 places uneaten cake `92:0`. M245 places wall sign `68:5` with Packet130 text. M246 places spruce log `17:1`. M247 places birch log `17:2`. M248 places orange wool `35:1`. M249 places yellow wool `35:4`. M250 places red wool `35:14`. M251 places black wool `35:15`. M252 places blue wool `35:11`. M253 places green wool `35:13`. M254 places still water `9:0` from bucket `326`. M255 places still lava `11:0` from bucket `327`. M256 spawns chest minecart Packet23 type `11`. M257 spawns furnace minecart Packet23 type `12`. M258 air-uses bread `297` to heal Packet8 `15 -> 20`. M259 air-uses cooked pork `320` to heal Packet8 `12 -> 20`. M260 air-uses apple `260` to heal Packet8 `16 -> 20`. M261 air-uses golden apple `322` to heal Packet8 `10 -> 20`. M262 air-uses cookie `357` to heal Packet8 `19 -> 20`. M263 air-uses mushroom stew `282` to heal Packet8 `12 -> 20` and leave bowl `281`. M264 air-uses raw porkchop `319` to heal Packet8 `17 -> 20`. M265 air-uses raw fish `349` to heal Packet8 `18 -> 20`. M266 air-uses cooked fish `350` to heal Packet8 `15 -> 20`. M267 air-uses milk bucket `335` to empty bucket `325` with no heal. M268 places fire `51:0` with flint-and-steel `259`. M269 shears oak leaves `18:8` for Packet21 leaf drop. M270 equips iron helmet `306` into armor slot 5. M271 equips gold chestplate `315` into armor slot 6. M272 equips diamond leggings `312` into armor slot 7. M273 equips chain boots `305` into armor slot 8. M274 settles falling gravel one cell after support removal. M275 cactus contact drops Packet8 health `20 -> 19`. M276 netherrack fire drops Packet8 health `20 -> 19`. M277 empty-hand Packet15 opens wooden door `64:0/64:8` to `64:4/64:12`. M278 empty-hand Packet15 opens trapdoor `96:3` to `96:7`. M279 empty-hand Packet15 pulses stone button `77:1` to `77:9` and back. M280 places magenta wool `35:2`. M281 places light-blue wool `35:3`. M282 places lime wool `35:5`. M283 places pink wool `35:6`. M284 places gray wool `35:7`. M285 places light-gray wool `35:8`. M286 places cyan wool `35:9`. M287 places purple wool `35:10`. M288 places brown wool `35:12`. M289 plants spruce sapling `6:1` on dirt. M290 plants birch sapling `6:2` on dirt. M291 places spruce leaves `18:9` beside spruce log `17:1`. M292 places birch leaves `18:10` beside oak log. M293 places sticky piston `29:1`. M294 places piston `33:1`. M295 powers wooden plate `72` and stone plate `70`. M296 smelts iron `15→265`, gold `14→266`, and pork `319→320`. M297 crafts planks `5`, sticks `280`, and torches `50`. M298 crafts wooden tools `268+270+271+269+290`. M299 crafts stone tools `272+273+274+275+291`. M300 pick-harvests cobble, coal, and diamond. M301 axe-harvests oak, spruce, and birch logs. M302 shovel-harvests dirt, sand, gravel, and clay. M303 harvests wheat, sugar cane, and cactus. M304 hoes dirt to farmland and tramples it back. M305 grows wheat, cactus, and sugar cane. M306 closes wooden door `64` and trapdoor `96`. M307 records drowning, suffocation, and lava hurt. M308 breaks ice and glass and melts ice to water. M309 powers detector rail `28:8` and powered rail `27:8`. M310 mounts boat type 1 and minecart type 10. M311 opens chest-minecart windows and spawns furnace minecarts.
M71-M110 remain frozen Aero laboratory evidence; no new Aero milestone is
planned before the vanilla foundation is complete.
