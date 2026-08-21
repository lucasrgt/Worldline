# M76 renderer decomposition

Status: GO in Worldline v1.64.0.

M76 decomposes the constant sixteen-block-entity M74 fixture into three exact
client treatments: `no-dispatch` removes the registered block-entity renderer,
`dispatch-only` retains sixteen renderer/probe calls but suppresses the nested
Aero call, and `aero16` retains both sixteen renderer calls and sixteen real
Aero at-rest calls. Server content, explicit synchronization, plan, nonce,
camera, heap, recorder, and window remain configured alike within each triplet.

Treatment setup occurs after fixture readiness and before the first retained
HEAD-to-HEAD interval. If M74's baseline hook ran first at that HEAD, M76 rejects
any retained/pending state, clears only the empty baseline, applies treatment,
and starts the real baseline at the following HEAD. The client must observe
vanilla `fpsLimit=0` and `Aero_FramePacer.ENABLED=false`; both are runtime gates.

Every binary record proves synchronized state `16/16`, identity mask `0xffff`,
visible chunks, and the treatment's exact structural pair: renderer/Aero calls
`0/0`, `16/0`, or `16/16`. Two fresh triplets reverse order. Each has one nonce
and one concrete plan shared across its three fresh server/client JVM pairs.

The qualified temporal observations were mixed. Triplet-one medians were
`994500/975200/969800 ns`; triplet-two medians were
`792100/1602200/1237300 ns` for no-dispatch/dispatch-only/aero16. M76 therefore
does not establish a stable stage ordering or isolate renderer/Aero cost. It
qualifies the decomposition and complete-census acquisition only.

Nonclaims: causal attribution, stable stage cost, regression or improvement,
statistical significance, independent frame samples, pixel visibility,
cross-machine generality, combat relation, or historical lag reproduction.
