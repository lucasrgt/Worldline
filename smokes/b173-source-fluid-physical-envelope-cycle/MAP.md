<!-- worldline-map-schema=1 -->
<!-- boundary=b173-source-fluid-physical-envelope-cycle -->
<!-- nonclaims=flowing-fluids,tick-policy,neighbor-response,break-transition,drop-matrix,gameplay-placement,save-reload,native-render -->
<!-- frozen-trace=26284ff2a2903c093b4b65f1b625a1d369693393a7ee368cffa09e87e9bc94ba -->

# Beta 1.7.3 source-fluid physical envelopes

One catalog covers the placed still-water and still-lava source states. Two state-domain rows
prove the singleton source metadata reached by gameplay placement, two collision rows compare
air controls with passable level, half-step, and full-step trajectories, and two light rows
observe per-fluid source-cell emission and skylight attenuation. Every row uses canonical public evidence,
an isolated official-server world, and a fresh-login persistence boundary.
The frozen light envelope records water as block light 0/skylight 12 and lava as block light
15/skylight 0; this distinction comes from the official server rather than a shared fluid default.

Flowing metadata, scheduled propagation, neighbor reactions, breaking, drops, the independent
gameplay-placement and save-reload census templates, and native rendering remain outside scope.

Frozen aggregate signal:
`family=source-fluid-physical-envelope,subjects=2,claims=6,layers=3,reload=FRESH_LOGINx6,state=08a39f4392d26e7a98085d1752edbd345eb3c44437148113275bbb9ba4646a46,collision=5bfdc6f75f2127693a48c4abac6de2bb01e6f5f0319a72bb50483f92b80c836a,light=9e214579b8c6e76a9f8837552821f7d80999785be2167e66086f80cbba5579f0`.

Qualified semantic signature:
`26284ff2a2903c093b4b65f1b625a1d369693393a7ee368cffa09e87e9bc94ba`.
