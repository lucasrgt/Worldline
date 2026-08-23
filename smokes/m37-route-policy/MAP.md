<!-- worldline-map-schema=1 -->
<!-- boundary=movement-route -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4a9a43b61c171fd05ab6156b07c963b7c1ebcdedc6ab7ea42d7a40db04cdf649 -->

# M37 Route Correction Policy

Two fresh protocol-14 sessions submit a three-step route with explicit
`STOP_ON_CORRECTION`. The first `+0.125 X` step is unchallenged. The second
targets a cache-selected solid terrain cube and must be corrected. The route
must return immediately with two outcomes, one correction, and no retry; the
third `+0.125 X` step is never sent.

The original cached chunk remains loaded. Official player NBT after clean
disconnect/save must equal the first step's pose, proving the absent third step
did not change server state.

Frozen expected signature SHA-256: `4a9a43b61c171fd05ab6156b07c963b7c1ebcdedc6ab7ea42d7a40db04cdf649`
