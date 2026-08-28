<!-- worldline-map-schema=1 -->
<!-- boundary=b173-source-fluid-physical-envelope-cycle -->
<!-- nonclaims=flowing-fluids,tick-policy,neighbor-response,break-transition,drop-matrix,gameplay-placement,save-reload,native-render -->
<!-- frozen-trace=e98412dd0167d6dd82ecebbbab9eea06f2e64517a2f673c317ef1319d0a27511 -->

# Beta 1.7.3 source-fluid physical envelopes

One catalog covers the placed still-water and still-lava source states. Two state-domain rows
prove the singleton source metadata reached by gameplay placement, two collision rows compare
air controls with passable level, half-step, and full-step trajectories, and two light rows
observe per-fluid source-cell emission and skylight attenuation. Every row uses canonical public evidence,
an isolated official-server world, and a fresh-login persistence boundary.

Flowing metadata, scheduled propagation, neighbor reactions, breaking, drops, the independent
gameplay-placement and save-reload census templates, and native rendering remain outside scope.

Frozen aggregate signal:
`family=source-fluid-physical-envelope,subjects=2,claims=6,layers=3,reload=FRESH_LOGINx6,state=0000000000000000000000000000000000000000000000000000000000000000,collision=0000000000000000000000000000000000000000000000000000000000000000,light=0000000000000000000000000000000000000000000000000000000000000000`.

Qualified semantic signature:
`e98412dd0167d6dd82ecebbbab9eea06f2e64517a2f673c317ef1319d0a27511`.
