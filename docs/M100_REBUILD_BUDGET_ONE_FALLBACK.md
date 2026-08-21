# M100 rebuild-budget-one fallback

Status: GO in Worldline v1.88.0.

M100 runs the exact four-page M74/M78 scene with cache1, TTL100000, and a
single page rebuild per frame. Runtime validation proves the literal settings;
vanilla FPS remains unlimited and Aero pacing remains disabled.

Complete records alternate between two exact modes. One draws two cached/page
calls and four instances directly; the next draws one page call and ten
instances directly. Both have one rebuild, cache1, and capacity-eviction delta
one. The M74 census independently reports direct renderer/list counters 4/4 or
10/10 at the same record index. Both modes are required and balanced to within
one record.

The alternation reflects the retained cache key meeting the sorted page loop:
a first-page hit leaves the rebuild for the second page, while a retained
second page is evicted when the first page consumes the next frame's budget.
This claim is limited to the exact fixture and pinned ordering.

Nonclaims: other budgets, page-order independence, other membership layouts,
unlimited or zero cache, TTL expiry, generic content, uninstrumented/additive
cost, causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
