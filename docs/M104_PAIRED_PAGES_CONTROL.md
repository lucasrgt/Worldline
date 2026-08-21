# M104 paired pages control

Status: GO in Worldline v1.92.0.

M104 turns the M101/M102 paged path and the M103 pages-disabled immediate
path into a direct paired structural comparison. It runs two balanced pairs:
enabled then disabled, followed by disabled then enabled. The two arms inside
each pair use the same server-authored plan, nonce, exact camera, sixteen
synchronized identities, property literals, and unified recorder schema.

With pages enabled, every retained record must contain sixteen queue entries,
four page calls and rebuilds, one cached page, eviction growth by four, and no
immediate calls. With pages disabled, every record must contain sixteen calls
to the exact immediate overload and zero queued/page/rebuild/cache/eviction
counters. The aligned M74 counters independently distinguish page rendering
from immediate at-rest rendering.

M104 also hardens the inherited terrain fixture: automatic plans include the
camera's support column before teleporting. The strict X/Y/Z/yaw/pitch gate
is unchanged; the hardening prevents gravity from making readiness flaky.

The paired timing summaries are observations only. M104 does not estimate
uninstrumented or additive cost and does not classify either arm as faster.

Nonclaims: causality, regression/improvement, statistical inference,
significance, pixels, cross-machine generality, combat, or historical lag
reproduction/attribution.
