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
| GUI tree | Neutral inventory Game UI tree with official-JAR match | GO - stable milestone |
| Invariant engine | Six fail-closed rules on live `watch(standard(runtime))` | GO - stable milestone |
| Semantic mappings | Closed 24-category catalog, adapter manifests, and static role graph | GO - stable milestone |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v1.122.0 direction

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
official portal activation, M133's live traversal and M134's complete roundtrip
into respawn transitions, additional
physics/worldgen,
entity AI, dimensions
and persistence/multiplayer composition.
M71-M110 remain frozen Aero laboratory evidence; no new Aero milestone is
planned before the vanilla foundation is complete.
