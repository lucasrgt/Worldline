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
| GUI tree | Neutral inventory Game UI tree with official-JAR match | GO - stable milestone |
| Invariant engine | Six fail-closed rules on live `watch(standard(runtime))` | GO - stable milestone |
| Semantic mappings | Closed 24-category catalog, adapter manifests, and static role graph | GO - stable milestone |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v1.63.0 direction

M75 qualifies exact multi-level Aero exposure acquisition while holding the
sixteen-entity scene constant. Its two mirrored ladders remain feasibility
evidence, not a monotonic or causal estimate. The next analytical step should
inspect whether both ladders show a stable ordering and define an estimand before
expanding repetitions; a larger campaign needs preregistered sample size and
machine/order controls. M71-M75 do not establish causal spike attribution.
Generic synchronization, persistence, multiple clients, merging, shift clicks,
and broad automation remain outside the stable contract.
